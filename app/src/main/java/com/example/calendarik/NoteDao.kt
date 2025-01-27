package com.example.calendarik

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE date = :date ORDER BY startTime")
    fun getNotesForDate(date: LocalDate): Flow<List<Note>>

    @Insert
    fun insert(note: Note): Long

    @Update
    fun update(note: Note): Int

    @Delete
    fun delete(note: Note): Int
}