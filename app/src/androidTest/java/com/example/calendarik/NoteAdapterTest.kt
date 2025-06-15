package com.example.calendarik

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class NoteAdapterTest {

    private lateinit var adapter: NoteAdapter
    private var editNote: Note? = null
    private var deleteNote: Note? = null

    @Before
    fun setup() {
        val listener = object : NoteActionListener {
            override fun onEdit(note: Note) { editNote = note }
            override fun onDelete(note: Note) { deleteNote = note }
        }
        adapter = NoteAdapter(listener)
    }

    @Test
    fun bind_showsCorrectData() {
        val note = Note(
            id = 1,
            eventName = "Test Event",
            noteText = "Test Description",
            date = LocalDate.now(),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 0),
            category = "design",
            reminderEnabled = false
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.note_item, null, false)

        val holder = NoteAdapter.NoteViewHolder(view)
        holder.bind(note, adapter.listener)

        assertEquals("Test Event", holder.eventNameTextView.text)
        assertEquals("Test Description", holder.noteTextView.text)
        assertEquals("10:00 - 11:00", holder.timeTextView.text)
    }
}