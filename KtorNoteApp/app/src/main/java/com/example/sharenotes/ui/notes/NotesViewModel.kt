package com.example.sharenotes.ui.notes

import androidx.lifecycle.*
import com.example.sharenotes.data.entities.Note
import com.example.sharenotes.repository.NoteRepository
import com.example.sharenotes.utils.Event
import com.example.sharenotes.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel
@Inject
constructor(
    private val repository: NoteRepository
):ViewModel(),LifecycleObserver {

    private val _forceUpdate= MutableLiveData<Boolean>(false)

    private val _allNotes= _forceUpdate.switchMap {
        repository.getAllNotes().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Event(it))
    }

    val allNotes: LiveData<Event<Resource<List<Note>>>> = _allNotes

    fun syncAllNotes()=_forceUpdate.postValue(true)

    fun insertNote(note: Note)=viewModelScope.launch {
        repository.insertNote(note)
    }

    fun deleteNote(noteID:String)=viewModelScope.launch {
        repository.deleteNote(noteID)
    }

    fun deleteLocallyDeletedNoteID(deleteNoteID:String)=viewModelScope.launch {
        repository.deleteLocallyDeletedNoteId(deleteNoteID)
    }
}