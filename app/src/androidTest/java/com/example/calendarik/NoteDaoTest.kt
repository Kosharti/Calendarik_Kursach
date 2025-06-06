package com.example.calendarik

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        noteDao = database.noteDao()
    }

    private fun createTestNote(
        id: Long = 0,
        eventName: String = "Test",
        noteText: String = "Test note",
        date: LocalDate = LocalDate.now(),
        startTime: LocalTime = LocalTime.of(12, 0),
        endTime: LocalTime = LocalTime.of(13, 0),
        category: String = "test",
        reminderEnabled: Boolean = false
    ): Note {
        return Note(
            id = id,
            eventName = eventName,
            noteText = noteText,
            date = date,
            startTime = startTime,
            endTime = endTime,
            category = category,
            reminderEnabled = reminderEnabled
        )
    }

    @Test
    fun insertAndGetNote() = runBlocking {
        val note = createTestNote()
        val id = noteDao.insert(note)

        val loaded = noteDao.getNoteById(id).first()
        assertEquals("Test", loaded?.eventName)
    }

    @Test
    fun getNotesForDate_returnsCorrectNotes() = runBlocking {
        val date = LocalDate.of(2023, 6, 15)
        val note1 = createTestNote(date = date)
        val note2 = createTestNote(date = date.plusDays(1))

        noteDao.insert(note1)
        noteDao.insert(note2)

        val notes = noteDao.getNotesForDate(date).first()
        assertEquals(1, notes.size)
        assertEquals(date, notes[0].date)
    }

    @After
    fun closeDb() {
        database.close()
    }
}