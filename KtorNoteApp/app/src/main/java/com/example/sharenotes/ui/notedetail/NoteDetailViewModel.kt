package com.example.sharenotes.ui.notedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharenotes.repository.NoteRepository
import com.example.sharenotes.utils.Event
import com.example.sharenotes.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel
@Inject constructor(
    private val noteRepository: NoteRepository
):ViewModel() {

    private val _addOwnerStatus=MutableLiveData<Event<Resource<String>>>()
    val addOwnerStatus:LiveData<Event<Resource<String>>> =_addOwnerStatus

    fun addOwnerToNote(owner:String,noteId: String){
        _addOwnerStatus.postValue(Event(Resource.Loading(null)))
        if (owner.isEmpty() || noteId.isEmpty()) {
            _addOwnerStatus.postValue(Event(Resource.Error(null,"The owner cannot be empty")))
            return
        }
        viewModelScope.launch {
            val result=noteRepository.addOwnerToNote(owner,noteId)
            _addOwnerStatus.postValue(Event(result))
        }
    }

    fun observeNoteBy(noteId:String)=noteRepository.observeByNoteId(noteId)
}