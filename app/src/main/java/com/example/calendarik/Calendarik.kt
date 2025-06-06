package com.example.calendarik

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.Locale

class Calendarik : AppCompatActivity(), NoteActionListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var selectedDate: LocalDate
    private lateinit var viewModel: MainViewModel
    private lateinit var notesAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendarik)

        val factory = MainViewModel.Factory(application)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        monthYearText = findViewById(R.id.monthYearText)
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        val addNoteButton: FloatingActionButton = findViewById(R.id.addNoteButton)

        selectedDate = LocalDate.now()
        setMonthView()

        notesAdapter = NoteAdapter(this)
        notesRecyclerView.adapter = notesAdapter
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        viewModel.notes.observe(this, Observer { notes ->
            notesAdapter.submitList(notes)
        })

        val prevButton: ImageView = findViewById(R.id.prevButton)
        prevButton.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            viewModel.setSelectedDate(selectedDate)
            setMonthView()
        }

        val nextButton: ImageView = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {            selectedDate = selectedDate.plusMonths(1)
            viewModel.setSelectedDate(selectedDate)
            setMonthView()
        }

        addNoteButton.setOnClickListener {
            val bottomSheetDialogFragment = AddNoteBottomSheetDialogFragment.newInstance(selectedDate)
            bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
        }

        viewModel.getAllNotesForMonth(selectedDate).observe(this) { notes ->
            val notesMap = notes.groupBy { it.date }
            val adapter = calendarRecyclerView.adapter as? CalendarAdapter
            if (adapter == null) {
                val newAdapter = CalendarAdapter(daysInMonthArray(selectedDate), selectedDate, notesMap,
                    onItemClick = { clickedDate ->
                        selectedDate = clickedDate
                        viewModel.setSelectedDate(selectedDate)
                    },
                    onMonthChange = { date ->
                        selectedDate = date
                        viewModel.setSelectedDate(selectedDate)
                        setMonthView()
                    })
                calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
                calendarRecyclerView.adapter = newAdapter
            } else {
                adapter.updateDays(daysInMonthArray(selectedDate), selectedDate)
                adapter.updateNotesMap(notesMap)
            }
        }

        viewModel.selectedDate.observe(this) { date ->
            selectedDate = date
            setMonthView()
        }

    }

    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate)
        val adapter = calendarRecyclerView.adapter as? CalendarAdapter
        if (adapter != null) {
            adapter.updateDays(daysInMonthArray(selectedDate), selectedDate)
        }
    }


    private fun daysInMonthArray(date: LocalDate): ArrayList<LocalDate?> {
        val daysInMonthArray = ArrayList<LocalDate?>()

        val yearMonth = YearMonth.from(date)
        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value

        val daysBefore = dayOfWeek - 1

        val prevMonth = yearMonth.minusMonths(1)
        val prevMonthLength = prevMonth.lengthOfMonth()
        for (i in daysBefore downTo 1) {
            daysInMonthArray.add(prevMonth.atDay(prevMonthLength - i + 1))
        }

        val daysInMonth = yearMonth.lengthOfMonth()
        for (i in 1..daysInMonth) {
            daysInMonthArray.add(yearMonth.atDay(i))
        }

        val totalDays = 42
        val daysAfter = totalDays - daysInMonthArray.size
        val nextMonth = yearMonth.plusMonths(1)
        for (i in 1..daysAfter) {
            daysInMonthArray.add(nextMonth.atDay(i))
        }

        return daysInMonthArray
    }


    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM\nyyyy", Locale.getDefault())
        return date.format(formatter)
    }

    override fun onEdit(note: Note) {
        Log.d("Calendarik", "Edit note with ID: ${note.id}")
        val bottomSheetDialogFragment = AddNoteBottomSheetDialogFragment.newInstance(note.id)
        bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
    }

    override fun onDelete(note: Note) {
        Log.d("Calendarik", "Delete note with ID: ${note.id}")
        viewModel.deleteNote(note)
    }
}