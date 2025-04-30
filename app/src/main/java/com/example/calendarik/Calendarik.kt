package com.example.calendarik

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.Locale
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import java.time.DayOfWeek

class Calendarik : AppCompatActivity() {

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

        notesAdapter = NoteAdapter { note ->
            viewModel.deleteNote(note)
        }
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
        nextButton.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            viewModel.setSelectedDate(selectedDate)
            setMonthView()
        }

        addNoteButton.setOnClickListener {
            val intent = Intent(this, com.example.calendarik.AddNoteActivity::class.java)
            intent.putExtra("selectedDate", selectedDate.toString())
            startActivity(intent)
        }

    }

    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate)
        val daysInMonth = daysInMonthArray(selectedDate)
        viewModel.getAllNotesForMonth(selectedDate).observe(this, Observer { notes ->
            val notesMap = notes.groupBy { it.date }

            val adapter = CalendarAdapter(daysInMonth, selectedDate, notesMap) { clickedDate ->
                selectedDate = clickedDate
                viewModel.setSelectedDate(selectedDate)
            }
            val layoutManager = GridLayoutManager(this, 7)
            calendarRecyclerView.layoutManager = layoutManager
            calendarRecyclerView.adapter = adapter
        })
    }


    private fun daysInMonthArray(date: LocalDate): ArrayList<LocalDate?> {
        val daysInMonthArray = ArrayList<LocalDate?>()

        val yearMonth = YearMonth.from(date)
        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value

        val daysBefore = dayOfWeek - 1
        var prevMonthDate = firstOfMonth.minusDays(daysBefore.toLong())

        for (i in 1..daysBefore) {
            daysInMonthArray.add(prevMonthDate)
            prevMonthDate = prevMonthDate.plusDays(1)
        }

        val daysInMonth = yearMonth.lengthOfMonth()
        for (i in 1..daysInMonth) {
            daysInMonthArray.add(LocalDate.of(date.year, date.monthValue, i))
        }

        val daysAfter = 35 - daysInMonthArray.size

        var nextMonthDate = LocalDate.of(date.year, date.monthValue, daysInMonth).plusDays(1)
        for (i in 1..daysAfter) {
            daysInMonthArray.add(nextMonthDate)
            nextMonthDate = nextMonthDate.plusDays(1)
        }

        return daysInMonthArray
    }


    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM\nyyyy", Locale.getDefault())
        return date.format(formatter)
    }

}