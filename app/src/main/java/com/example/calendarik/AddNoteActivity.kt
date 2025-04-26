package com.example.calendarik

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.ChipGroup
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog

class AddNoteActivity : AppCompatActivity() {

    private lateinit var eventNameEditText: EditText
    private lateinit var noteTextEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var dateButton: Button
    private lateinit var startTimeButton: Button
    private lateinit var endTimeButton: Button
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var reminderSwitch: Switch
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var createEventButton: Button
    private lateinit var selectedDate: LocalDate
    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val factory = MainViewModel.Factory(application)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        eventNameEditText = findViewById(R.id.eventNameEditText)
        noteTextEditText = findViewById(R.id.noteTextEditText)
        dateTextView = findViewById(R.id.dateTextView)
        dateButton = findViewById(R.id.dateButton)
        startTimeButton = findViewById(R.id.startTimeButton)
        endTimeButton = findViewById(R.id.endTimeButton)
        startTimeTextView = findViewById(R.id.startTimeTextView)
        endTimeTextView = findViewById(R.id.endTimeTextView)
        reminderSwitch = findViewById(R.id.reminderSwitch)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        createEventButton = findViewById(R.id.createEventButton)

        // Убираем подсказки после начала ввода
        eventNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                eventNameEditText.hint = null
            } else if (eventNameEditText.text.isNullOrEmpty()) {
                eventNameEditText.hint = "Event Name*"
            }
        }

        noteTextEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                noteTextEditText.hint = null
            } else if (noteTextEditText.text.isNullOrEmpty()) {
                noteTextEditText.hint = "Type the note here..."
            }
        }

        val dateString = intent.getStringExtra("selectedDate")
        selectedDate = if (dateString != null) {
            LocalDate.parse(dateString)
        } else {
            LocalDate.now()
        }
        updateDateTextView()
        dateButton.setOnClickListener { showDatePickerDialog() }
        startTimeButton.setOnClickListener { showTimePickerDialog(true) }
        endTimeButton.setOnClickListener { showTimePickerDialog(false) }

        createEventButton.setOnClickListener { createEvent() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
            updateDateTextView()
        }, year, month, day)
        dpd.show()
    }

    private fun showTimePickerDialog(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(this, { _, hourOfDay, minute ->
            val time = LocalTime.of(hourOfDay, minute)
            if (isStartTime) {
                selectedStartTime = time
                startTimeTextView.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                selectedEndTime = time
                endTimeTextView.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }, hour, minute, true)
        tpd.show()
    }

    private fun updateDateTextView() {
        dateTextView.text = "Date: ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
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

        finish()
    }
}