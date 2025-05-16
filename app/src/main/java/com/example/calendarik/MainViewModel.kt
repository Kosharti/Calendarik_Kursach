package com.example.calendarik

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = AppDatabase.getDatabase(application).noteDao()
    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _notes = MediatorLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    init {
        _notes.addSource(_selectedDate) { date ->
            viewModelScope.launch {
                noteDao.getNotesForDate(date).collect { notes ->
                    _notes.value = notes
                }
            }

        }
    }


    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insert(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.update(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.delete(note)
        }
    }

    fun getNoteById(id: Long): LiveData<Note> {  // Добавлено
        return noteDao.getNoteById(id).asLiveData()
    }

    fun getAllNotesForMonth(date: LocalDate): LiveData<List<Note>> {
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = date.withDayOfMonth(date.lengthOfMonth())
        return noteDao.getNotesForMonth(startOfMonth, endOfMonth).asLiveData()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

