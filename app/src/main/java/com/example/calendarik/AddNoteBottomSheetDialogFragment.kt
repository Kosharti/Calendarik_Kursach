package com.example.calendarik

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import java.time.*
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

    private val EXACT_ALARM_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = arguments?.getLong("noteId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_add_note, container, false)

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
        selectedDate = if (dateString != null) LocalDate.parse(dateString) else LocalDate.now()
        updateDateEditText()

        dateEditText.setOnClickListener { showDatePickerDialog() }
        dateButton.setOnClickListener { showDatePickerDialog() }
        startTimeEditText.setOnClickListener { showTimePickerDialog(true) }
        startTimeButton.setOnClickListener { showTimePickerDialog(true) }
        endTimeEditText.setOnClickListener { showTimePickerDialog(false) }
        endTimeButton.setOnClickListener { showTimePickerDialog(false) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 999)
            }
        }

        noteId?.let {
            viewModel.getNoteById(it).observe(viewLifecycleOwner) { note ->
                eventNameEditText.setText(note.eventName)
                noteTextEditText.setText(note.noteText)
                dateEditText.setText(note.date.toString())
                selectedDate = note.date
                selectedStartTime = note.startTime
                selectedEndTime = note.endTime
                startTimeEditText.setText(note.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "")
                endTimeEditText.setText(note.endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "")
                reminderSwitch.isChecked = note.reminderEnabled
                when (note.category) {
                    "Brainstorm" -> categoryChipGroup.check(R.id.brainstormChip)
                    "Design" -> categoryChipGroup.check(R.id.designChip)
                    "Workout" -> categoryChipGroup.check(R.id.workoutChip)
                }
            }
        }

        createEventButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SCHEDULE_EXACT_ALARM)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Exact Alarm Permission Required")
                        .setMessage("This app needs exact alarm permission to set reminders accurately.")
                        .setPositiveButton("OK") { _, _ -> requestExactAlarmPermission() }
                        .setNegativeButton("Cancel", null).show()
                } else requestExactAlarmPermission()
            } else createEvent()
        }
    }

    private fun updateDateEditText() {
        dateEditText.setText(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    private fun showDatePickerDialog() {
        val now = Calendar.getInstance()
        val dpd = DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDate = LocalDate.of(y, m + 1, d)
            updateDateEditText()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        dpd.show()
    }

    private fun showTimePickerDialog(isStart: Boolean) {
        val now = Calendar.getInstance()
        val tpd = TimePickerDialog(requireContext(), { _, h, m ->
            val time = LocalTime.of(h, m)
            if (isStart) {
                selectedStartTime = time
                startTimeEditText.setText(time.format(DateTimeFormatter.ofPattern("HH:mm")))
            } else {
                selectedEndTime = time
                endTimeEditText.setText(time.format(DateTimeFormatter.ofPattern("HH:mm")))
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true)
        tpd.show()
    }

    private fun createEvent() {
        val name = eventNameEditText.text.toString()
        val text = noteTextEditText.text.toString()
        val category = when (categoryChipGroup.checkedChipId) {
            R.id.brainstormChip -> "Brainstorm"
            R.id.designChip -> "Design"
            R.id.workoutChip -> "Workout"
            else -> "Other"
        }
        val remind = reminderSwitch.isChecked

        val note = Note(
            id = noteId ?: 0,
            eventName = name,
            noteText = text,
            date = selectedDate,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            category = category,
            reminderEnabled = remind
        )

        if (noteId == null || noteId == 0L) viewModel.insertNote(note)
        else viewModel.updateNote(note)

        scheduleNotification(name, selectedDate, selectedStartTime, remind, noteId ?: System.currentTimeMillis())
        viewModel.setSelectedDate(selectedDate)
        dismiss()
    }

    private fun scheduleNotification(eventName: String, date: LocalDate, time: LocalTime?, enabled: Boolean, noteId: Long) {
        if (!enabled) return

        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        intent.putExtra("eventName", eventName)
        intent.putExtra("noteId", noteId)

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), (noteId % Int.MAX_VALUE).toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val ldt = time?.let { LocalDateTime.of(date, it) } ?: LocalDateTime.of(date, LocalTime.of(9, 0))
        val triggerTime = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM), EXACT_ALARM_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXACT_ALARM_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createEvent()
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(intent)
        }
    }

    companion object {
        fun newInstance(selectedDate: LocalDate? = null): AddNoteBottomSheetDialogFragment {
            val f = AddNoteBottomSheetDialogFragment()
            f.arguments = Bundle().apply { putString("selectedDate", selectedDate?.toString()) }
            return f
        }

        fun newInstance(noteId: Long): AddNoteBottomSheetDialogFragment {
            val f = AddNoteBottomSheetDialogFragment()
            f.arguments = Bundle().apply { putLong("noteId", noteId) }
            return f
        }
    }
}