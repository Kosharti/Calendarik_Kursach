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
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.SwitchDefaults

class AddNoteBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var eventNameEditText: EditText
    private lateinit var noteTextEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var endTimeEditText: EditText
    private lateinit var reminderSwitchCompose: ComposeView
    private var isReminderEnabled = false
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var createEventButton: Button
    private lateinit var selectedDate: LocalDate
    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null
    private lateinit var viewModel: MainViewModel
    private var noteId: Long? = null

    private val EXACT_ALARM_PERMISSION_CODE = 123
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

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
        reminderSwitchCompose = view.findViewById(R.id.reminderSwitchCompose)
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup)
        createEventButton = view.findViewById(R.id.createEventButton)

        setupReminderSwitch()
        setupChips()
        setupDateAndTimeInputs()
        setupButton()
        loadNoteIfEditing()
        checkNotificationPermission()
    }

    private fun setupReminderSwitch() {
        reminderSwitchCompose.setContent {
            MaterialTheme {
                var checked by remember { mutableStateOf(isReminderEnabled) }
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        isReminderEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCED3DE),
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF735BF2),
                        disabledUncheckedThumbColor = Color.White,
                        disabledUncheckedTrackColor = Color(0xFFCED3DE),
                        disabledCheckedThumbColor = Color.White,
                        disabledCheckedTrackColor = Color(0xFF735BF2),
                        uncheckedBorderColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }

    private fun setupChips() {
        val brainstormChip = requireView().findViewById<Chip>(R.id.brainstormChip)
        val designChip = requireView().findViewById<Chip>(R.id.designChip)
        val workoutChip = requireView().findViewById<Chip>(R.id.workoutChip)

        brainstormChip.setOnClickListener {
            categoryChipGroup.check(R.id.brainstormChip)
            brainstormChip.setChipBackgroundColorResource(R.color.purple_light)
            brainstormChip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.oval_1)
        }

        designChip.setOnClickListener {
            categoryChipGroup.check(R.id.designChip)
            designChip.setChipBackgroundColorResource(R.color.green_light)
            designChip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.oval_2)
        }

        workoutChip.setOnClickListener {
            categoryChipGroup.check(R.id.workoutChip)
            workoutChip.setChipBackgroundColorResource(R.color.blue_light)
            workoutChip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.oval_3)
        }
    }

    private fun setupDateAndTimeInputs() {
        val dateString = arguments?.getString("selectedDate")
        selectedDate = if (dateString != null) LocalDate.parse(dateString) else LocalDate.now()
        updateDateEditText()

        requireView().findViewById<ImageButton>(R.id.dateButton).setOnClickListener { showDatePickerDialog() }
        requireView().findViewById<ImageButton>(R.id.startTimeButton).setOnClickListener { showTimePickerDialog(true) }
        requireView().findViewById<ImageButton>(R.id.endTimeButton).setOnClickListener { showTimePickerDialog(false) }

        val timeInputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isDigit(source[i]) && source[i] != ':') {
                    return@InputFilter ""
                }
            }
            null
        }

        val dateInputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isDigit(source[i]) && source[i] != '-') {
                    return@InputFilter ""
                }
            }
            null
        }

        startTimeEditText.filters = arrayOf(timeInputFilter, InputFilter.LengthFilter(5))
        endTimeEditText.filters = arrayOf(timeInputFilter, InputFilter.LengthFilter(5))
        dateEditText.filters = arrayOf(dateInputFilter, InputFilter.LengthFilter(10))

        startTimeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2 && !s.contains(':')) {
                    s.insert(2, ":")
                }
                try {
                    val time = LocalTime.parse(s.toString(), timeFormat)
                    selectedStartTime = time
                    startTimeEditText.error = null

                    selectedEndTime?.let {
                        if (it.isBefore(time)) {
                            endTimeEditText.error = "End time must be after start time"
                        }
                    }
                } catch (e: Exception) {
                    if (s?.isNotBlank() == true) {
                        startTimeEditText.error = "Invalid time format (HH:mm)"
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        endTimeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2 && !s.contains(':')) {
                    s.insert(2, ":")
                }
                try {
                    val time = LocalTime.parse(s.toString(), timeFormat)
                    selectedEndTime = time
                    endTimeEditText.error = null

                    selectedStartTime?.let {
                        if (time.isBefore(it)) {
                            endTimeEditText.error = "End time must be after start time"
                        }
                    }
                } catch (e: Exception) {
                    if (s?.isNotBlank() == true) {
                        endTimeEditText.error = "Invalid time format (HH:mm)"
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dateEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.length == 4 && !s.contains('-')) {
                        s.insert(4, "-")
                    } else if (s.length == 7 && s.count { it == '-' } == 1) {
                        s.insert(7, "-")
                    }
                }

                try {
                    selectedDate = LocalDate.parse(s.toString(), dateFormat)
                    dateEditText.error = null
                } catch (e: Exception) {
                    if (s?.isNotBlank() == true) {
                        dateEditText.error = "Invalid date format (YYYY-MM-DD)"
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupButton() {
        createEventButton.setOnClickListener {

            view?.let { v ->
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }

            if (!validateInputs()) {
                Toast.makeText(context, "Please correct the errors", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

    private fun validateInputs(): Boolean {
        val name = eventNameEditText.text.toString()
        if (name.isBlank()) {
            eventNameEditText.error = "Event name is required"
            return false
        }

        try {
            selectedStartTime?.let { startTime ->
                selectedEndTime?.let { endTime ->
                    if (endTime.isBefore(startTime)) {
                        endTimeEditText.error = "End time must be after start time"
                        return false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("InputValidation", "Time validation error", e)
            return false
        }

        try {
            LocalDate.parse(dateEditText.text.toString(), dateFormat)
        } catch (e: Exception) {
            dateEditText.error = "Invalid date format"
            return false
        }

        return true
    }

    private fun loadNoteIfEditing() {
        noteId?.let {
            viewModel.getNoteById(it).observe(viewLifecycleOwner) { note ->
                if (note != null) {
                    eventNameEditText.setText(note.eventName)
                    noteTextEditText.setText(note.noteText)
                    selectedDate = note.date
                    dateEditText.setText(note.date.toString())
                    selectedStartTime = note.startTime
                    selectedEndTime = note.endTime
                    startTimeEditText.setText(note.startTime?.format(timeFormat) ?: "")
                    endTimeEditText.setText(note.endTime?.format(timeFormat) ?: "")
                    isReminderEnabled = note.reminderEnabled

                    reminderSwitchCompose.setContent {
                        MaterialTheme {
                            var checked by remember { mutableStateOf(isReminderEnabled) }
                            Switch(
                                checked = checked,
                                onCheckedChange = {
                                    checked = it
                                    isReminderEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCED3DE),
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF735BF2)
                                )
                            )
                        }
                    }

                    val categoryLower = note.category.trim().lowercase(Locale.getDefault())
                    when (categoryLower) {
                        "brainstorm" -> categoryChipGroup.check(R.id.brainstormChip)
                        "design" -> categoryChipGroup.check(R.id.designChip)
                        "workout" -> categoryChipGroup.check(R.id.workoutChip)
                        else -> categoryChipGroup.clearCheck()
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 999)
            }
        }
    }

    private fun updateDateEditText() {
        dateEditText.setText(selectedDate.format(dateFormat))
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
                startTimeEditText.setText(time.format(timeFormat))
            } else {
                selectedEndTime = time
                endTimeEditText.setText(time.format(timeFormat))
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true)
        tpd.show()
    }

    private fun createEvent() {
        val name = eventNameEditText.text.toString()
        val text = noteTextEditText.text.toString()

        val selectedChipId = categoryChipGroup.checkedChipId
        val category = when (selectedChipId) {
            R.id.brainstormChip -> "brainstorm"
            R.id.designChip -> "design"
            R.id.workoutChip -> "workout"
            else -> "other"
        }

        val note = Note(
            id = noteId ?: 0,
            eventName = name,
            noteText = text,
            date = selectedDate,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            category = category,
            reminderEnabled = isReminderEnabled
        )

        if (noteId == null || noteId == 0L) viewModel.insertNote(note)
        else viewModel.updateNote(note)

        scheduleNotification(name, selectedDate, selectedStartTime, isReminderEnabled, noteId ?: System.currentTimeMillis())
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