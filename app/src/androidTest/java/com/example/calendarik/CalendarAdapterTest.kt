package com.example.calendarik

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class CalendarAdapterTest {

    private lateinit var adapter: CalendarAdapter
    private lateinit var context: Context
    private val testDate = LocalDate.of(2025, 6, 15)
    private val days = ArrayList<LocalDate?>().apply {
        addAll(listOf(null, null, null, testDate, testDate.plusDays(1)))
    }
    private val notesMap = mapOf(
        testDate to listOf(
            Note(
                id = 1,
                eventName = "Test",
                noteText = "Test note",
                date = testDate,
                startTime = LocalTime.of(20, 30),
                endTime = LocalTime.of(20, 31),
                category = "design",
                reminderEnabled = false
            )
        )
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        adapter = CalendarAdapter(
            days = days,
            selectedDate = testDate,
            notesMap = notesMap,
            onItemClick = {},
            onMonthChange = {}
        )
    }

    @Test
    fun getItemCount_returnsCorrectSize() {
        assertEquals(days.size, adapter.itemCount)
    }

    @Test
    fun onBindViewHolder_setsCorrectData() {
        val parent = FrameLayout(context)
        parent.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.calendar_cell, parent, false)
        val holder = CalendarAdapter.CalendarViewHolder(view)

        adapter.onBindViewHolder(holder, 3)
        assertEquals(testDate.dayOfMonth.toString(), holder.dayOfMonth.text)
        assertEquals(1, holder.ringsContainer.childCount)

        adapter.onBindViewHolder(holder, 0)
        assertEquals("", holder.dayOfMonth.text)
    }

    @Test
    fun setSelectedDate_updatesSelectedDate() {
        val newDate = testDate.plusDays(1)
        adapter.setSelectedDate(newDate)
        assertEquals(newDate, adapter.getSelectedDate())
    }

    @Test
    fun updateDays_updatesData() {
        val newDays = ArrayList<LocalDate?>().apply {
            addAll(listOf(testDate, testDate.plusDays(1)))
        }
        adapter.updateDays(newDays, testDate)
        assertEquals(newDays.size, adapter.itemCount)
    }
}