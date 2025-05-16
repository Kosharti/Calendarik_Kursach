
package com.example.calendarik

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddNoteBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var eventNameEditText: EditText
    private lateinit var noteTextEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var endTimeEditText: EditText
    private lateinit var reminderSwitch: Switch
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var createEventButton: Button
    private lateinit var selectedDate: LocalDate
    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null
    private lateinit var viewModel: MainViewModel
    private var noteId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = arguments?.getLong("noteId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_add_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = MainViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        eventNameEditText = view.findViewById(R.id.eventNameEditText)
        noteTextEditText = view.findViewById(R.id.noteTextEditText)
        dateEditText = view.findViewById(R.id.dateTextView)
        startTimeEditText = view.findViewById(R.id.startTimeTextView)
        endTimeEditText = view.findViewById(R.id.endTimeTextView)
        reminderSwitch = view.findViewById(R.id.reminderSwitch)
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup)
        createEventButton = view.findViewById(R.id.createEventButton)

        val dateButton: ImageButton = view.findViewById(R.id.dateButton)
        val startTimeButton: ImageButton = view.findViewById(R.id.startTimeButton)
        val endTimeButton: ImageButton = view.findViewById(R.id.endTimeButton)

        val dateString = arguments?.getString("selectedDate")
        selectedDate = if (dateString != null) {
            LocalDate.parse(dateString)
        } else {
            LocalDate.now()
        }

        updateDateEditText()
        dateEditText.setOnClickListener { showDatePickerDialog() }
        dateButton.setOnClickListener { showDatePickerDialog() }
        startTimeEditText.setOnClickListener { showTimePickerDialog(true) }
        startTimeButton.setOnClickListener { showTimePickerDialog(true) }
        endTimeEditText.setOnClickListener { showTimePickerDialog(false) }
        endTimeButton.setOnClickListener { showTimePickerDialog(false) }

        // Add TextWatchers
        dateEditText.addTextChangedListener(DateTextWatcher(dateEditText))
        startTimeEditText.addTextChangedListener(TimeTextWatcher(startTimeEditText))
        endTimeEditText.addTextChangedListener(TimeTextWatcher(endTimeEditText))

        createEventButton.setOnClickListener { createEvent() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
            updateDateEditText()
        }, year, month, day)
        dpd.show()
    }

    private fun showTimePickerDialog(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            val time = LocalTime.of(hourOfDay, minute)
            val formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"))
            if (isStartTime) {
                selectedStartTime = time
                startTimeEditText.hint = formattedTime
            } else {
                selectedEndTime = time
                endTimeEditText.hint = formattedTime
            }
        }, hour, minute, true)
        tpd.show()
    }

    private fun updateDateEditText() {
        dateEditText.hint = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun createEvent() {
        val eventName = eventNameEditText.text.toString()
        val noteText = noteTextEditText.text.toString()
        val categoryId = categoryChipGroup.checkedChipId
        val category = when (categoryId) {
            R.id.brainstormChip -> "Brainstorm"
            R.id.designChip -> "Design"
            R.id.workoutChip -> "Workout"
            else -> "Other"
        }

        val reminderEnabled = reminderSwitch.isChecked

        if (noteId == null) {
            // Создаем новую заметку
            val note = Note(
                eventName = eventName,
                noteText = noteText,
                date = selectedDate,
                startTime = selectedStartTime,
                endTime = selectedEndTime,
                category = category,
                reminderEnabled = reminderEnabled
            )
            viewModel.insertNote(note)
        } else {
            // Обновляем существующую заметку
            val note = Note(
                id = noteId!!,
                eventName = eventName,
                noteText = noteText,
                date = selectedDate,
                startTime = selectedStartTime,
                endTime = selectedEndTime,
                category = category,
                reminderEnabled = reminderEnabled
            )
            viewModel.updateNote(note)
        }
        scheduleNotification(eventName, selectedDate, selectedStartTime, reminderEnabled)
        dismiss()
    }

    private fun scheduleNotification(eventName: String, date: LocalDate, time: LocalTime?, reminderEnabled: Boolean) {
        if (reminderEnabled) {
            val intent = Intent(requireContext(), NotificationReceiver::class.java)
            intent.putExtra("eventName", eventName)
            intent.putExtra("noteId", noteId?.toInt() ?: Random().nextInt()) // Pass noteId

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                noteId?.toInt() ?: Random().nextInt(), // Unique ID for each notification
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)

            if (time != null) {
                calendar.set(Calendar.HOUR_OF_DAY, time.hour)
                calendar.set(Calendar.MINUTE, time.minute)
                calendar.set(Calendar.SECOND, 0)
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 9) // Default hour
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }

            val notificationTime = calendar.timeInMillis

            if (notificationTime > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                    )
                }
                Toast.makeText(requireContext(), "Reminder set", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Invalid date or time for reminder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(selectedDate: LocalDate? = null): AddNoteBottomSheetDialogFragment {
            val fragment = AddNoteBottomSheetDialogFragment()
            val args = Bundle()
            args.putString("selectedDate", selectedDate?.toString())
            fragment.arguments = args
            return fragment
        }

        fun newInstance(noteId: Long): AddNoteBottomSheetDialogFragment {
            val fragment = AddNoteBottomSheetDialogFragment()
            val args = Bundle()
            args.putLong("noteId", noteId)
            fragment.arguments = args
            return fragment
        }
    }

    // TextWatchers for Date and Time input
    private class DateTextWatcher(private val editText: EditText) : TextWatcher {
        private var updating = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (updating) return

            updating = true
            val text = s.toString()
            val formattedText = formatDate(text)

            if (formattedText != text) {
                editText.setText(formattedText)
                editText.setSelection(formattedText.length) // Move cursor to the end
            }

            updating = false
        }

        private fun formatDate(text: String): String {
            val digitsOnly = text.filter { it.isDigit() }

            return when (digitsOnly.length) {
                in 0..4 -> digitsOnly
                5 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 5)}"
                in 6..6 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}"
                7 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}-${digitsOnly.substring(6, 7)}"
                in 8..8 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}-${digitsOnly.substring(6, 8)}"
                else -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}-${digitsOnly.substring(6, 8)}"
            }
        }
    }

    private class TimeTextWatcher(private val editText: EditText) : TextWatcher {
        private var updating = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (updating) return

            updating = true
            val text = s.toString()
            val formattedText = formatTime(text)

            if (formattedText != text) {
                editText.setText(formattedText)
                editText.setSelection(formattedText.length)
            }

            updating = false
        }

        private fun formatTime(text: String): String {
            val digitsOnly = text.filter { it.isDigit() }

            return when (digitsOnly.length) {
                in 0..2 -> digitsOnly
                3 -> "${digitsOnly.substring(0, 2)}:${digitsOnly.substring(2, 3)}"
                in 4..4 -> "${digitsOnly.substring(0, 2)}:${digitsOnly.substring(2, 4)}"
                else -> "${digitsOnly.substring(0, 2)}:${digitsOnly.substring(2, 4)}"
            }
        }
    }
}
