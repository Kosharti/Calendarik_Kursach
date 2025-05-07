
package com.example.calendarik

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
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
        dismiss()
    }

    companion object {
        fun newInstance(selectedDate: LocalDate? = null): AddNoteBottomSheetDialogFragment {
            val fragment = AddNoteBottomSheetDialogFragment()
            val args = Bundle()
            args.putString("selectedDate", selectedDate?.toString())
            fragment.arguments = args
            return fragment
        }
    }
}
