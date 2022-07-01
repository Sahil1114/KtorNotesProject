package com.example.sharenotes.ui.addedtinote

import androidx.lifecycle.*
import com.example.sharenotes.data.entities.Note
import com.example.sharenotes.repository.NoteRepository
import com.example.sharenotes.utils.Event
import com.example.sharenotes.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel
@Inject
constructor(
    private val repository: NoteRepository
):ViewModel(),LifecycleObserver {

    private  val _note=MutableLiveData<Event<Resource<Note>>>()
    val note:LiveData<Event<Resource<Note>>> = _note

    @DelicateCoroutinesApi
    fun insertNote(note: Note)=GlobalScope.launch {
        repository.insertNote(note)
    }

    fun getNoteById(noteId:String)=viewModelScope.launch {
        _note.postValue(Event(Resource.Loading(null)))
        val note=repository.getNoteById(noteId)

        note?.let {
            _note.postValue(Event(Resource.Success(note)))
        }?:_note.postValue(Event(Resource.Error(null,"Note not found")))
    }
}