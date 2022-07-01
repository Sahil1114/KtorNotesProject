package com.example.sharenotes.ui.auth

import androidx.lifecycle.*
import com.example.sharenotes.repository.NoteRepository
import com.example.sharenotes.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject
constructor(
    private val notesRepository: NoteRepository
):ViewModel() ,LifecycleObserver{

    private val _registerStatus = MutableLiveData<Resource<String>>();
    val registerStatus:LiveData<Resource<String>> = _registerStatus

    private val _loginStatus = MutableLiveData<Resource<String>>();
    val loginStatus:LiveData<Resource<String>> = _loginStatus

    fun login(email:String,password:String){
        _loginStatus.postValue(Resource.Loading(null))
        if(email.isEmpty()||password.isEmpty() ){
            _loginStatus.postValue(Resource.Error(null,"Please fill out all the entries"))
            return
        }


        viewModelScope.launch {
            val result=notesRepository.login(email,password)
            _loginStatus.postValue(result)
        }
    }

    fun register(email:String,password:String,repeatedPassword:String){
        _registerStatus.postValue(Resource.Loading(null))
        if(email.isEmpty()||password.isEmpty()||repeatedPassword.isEmpty()){
            _registerStatus.postValue(Resource.Error(null,"Please fill out all the entries"))
            return
        }
        if(password != repeatedPassword){
            _registerStatus.postValue(Resource.Error(null,"Password do not match"))
        }

        viewModelScope.launch {
            val result=notesRepository.register(email,password)
            _registerStatus.postValue(result)
        }
    }

}