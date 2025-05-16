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

        notesAdapter = NoteAdapter(this) // Pass the activity as the listener
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

    }

    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate)
        val daysInMonth = daysInMonthArray(selectedDate)

        viewModel.getAllNotesForMonth(selectedDate).observe(this, Observer { notes ->
            val notesMap = notes.groupBy { it.date }

            // Check if the adapter is already initialized
            if (calendarRecyclerView.adapter == null) {
                val adapter = CalendarAdapter(daysInMonth, selectedDate, notesMap,
                    onItemClick = { clickedDate ->
                        selectedDate = clickedDate
                        viewModel.setSelectedDate(selectedDate)
                    },
                    onMonthChange = { date ->  // Handle month change
                        selectedDate = date
                        viewModel.setSelectedDate(selectedDate)
                        setMonthView() // Refresh the calendar
                    })
                val layoutManager = GridLayoutManager(this, 7)
                calendarRecyclerView.layoutManager = layoutManager
                calendarRecyclerView.adapter = adapter
            } else {
                // If the adapter is initialized, update the data directly
                val adapter = calendarRecyclerView.adapter as CalendarAdapter
                adapter.setSelectedDate(selectedDate)
            }
        })
    }


    private fun daysInMonthArray(date: LocalDate): ArrayList<LocalDate?> {
        val daysInMonthArray = ArrayList<LocalDate?>()

        val yearMonth = YearMonth.from(date)
        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value // 1 (Monday) to 7 (Sunday)
        val daysBefore = if (dayOfWeek == 1) 6 else dayOfWeek - 1 // Days to subtract to reach previous month

        // Add dates from the previous month
        var prevMonth = date.minusMonths(1).withDayOfMonth(yearMonth.minusMonths(1).lengthOfMonth())
        for (i in 1..daysBefore) {
            daysInMonthArray.add(prevMonth)
            prevMonth = prevMonth.minusDays(1)
        }
        daysInMonthArray.reverse() // Reverse the list to get the correct order

        // Add dates from the current month
        val daysInMonth = yearMonth.lengthOfMonth()
        for (i in 1..daysInMonth) {
            daysInMonthArray.add(LocalDate.of(date.year, date.monthValue, i))
        }

        // Add dates from the next month
        val daysAfter = 42 - daysInMonthArray.size // Ensure a 6-week calendar (7 x 6 = 42)
        var nextMonth = date.plusMonths(1).withDayOfMonth(1)
        for (i in 1..daysAfter) {
            daysInMonthArray.add(nextMonth)
            nextMonth = nextMonth.plusDays(1)
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