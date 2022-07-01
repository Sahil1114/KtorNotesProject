package com.example.sharenotes.repository

import android.app.Application
import android.util.Log
import com.example.sharenotes.data.entities.LocallyDeletedNoteID
import com.example.sharenotes.data.local.NoteDao
import com.example.sharenotes.data.entities.Note
import com.example.sharenotes.data.remote.NoteApi
import com.example.sharenotes.data.remote.request.AccountRequest
import com.example.sharenotes.data.remote.request.AddOwnerRequest
import com.example.sharenotes.data.remote.request.DeleteNoteRequest
import com.example.sharenotes.utils.Resource
import com.example.sharenotes.utils.Status
import com.example.sharenotes.utils.checkForInternetConnection
import com.example.sharenotes.utils.networkBoundResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao:NoteDao,
    private val noteApi: NoteApi,
    private val context: Application
){

    private var currNotesResponse:Response<List<Note>>?=null

    suspend fun deleteLocallyDeletedNoteId(deletedNoteID:String){
        noteDao.deleteNoteById(deletedNoteID)
    }

    suspend fun deleteNote(noteId: String){
        val response = try {
            noteApi.deleteNote(DeleteNoteRequest(noteId))
        } catch (e: Exception) {
            null
        }
        noteDao.deleteNoteById(noteId)
        if(response==null||!response.isSuccessful){
            noteDao.insetLocallyDeletedNoteID(LocallyDeletedNoteID(noteId))
        }else{
            deleteLocallyDeletedNoteId(noteId)
        }
    }

    suspend fun insertNote(note:Note){
        val response=try {
            Timber.d("Notes Add")
            noteApi.addNote(note)
        }catch (e:Exception){
            Timber.d(e)
            null
        }
        if (response != null && response.isSuccessful) {
            Timber.d("Successful")
            noteDao.insertNote(note.apply { isSynced = true })
        } else {
            Timber.d("Not successful")
            noteDao.insertNote(note)
        }
    }

    suspend fun insertNotes(notes: List<Note>) {
        notes.forEach {
            insertNote(it)
        }
    }

    suspend fun syncNotes(){
        val locallyDeletedNoteID=noteDao.getAllLocallyDeletedNoteIDs()
        locallyDeletedNoteID.forEach {id->
            deleteNote(id.deletedNoteID)
        }

        val unsycedNotes=noteDao.getAllUnsyncedNotes()
        unsycedNotes.forEach{note->
            insertNote(note)
        }

        currNotesResponse=noteApi.getNotes()

        currNotesResponse?.body()?.let {notes->
            noteDao.deleteAllNotes()
            insertNotes(notes.onEach {note->
                note.isSynced=true
            })
        }
    }

    fun getAllNotes(): Flow<Resource<List<Note>>> =
        networkBoundResources(
            query = {
                    noteDao.getAllNotes()
            },
            fetch = {
                    syncNotes()
                    currNotesResponse
            },
            saveFetchResult = {response->
                    response?.body()?.let {
                         insertNotes(it.onEach {note ->
                             note.isSynced=true
                         })
                    }
            },
            shouldFetch = {
                checkForInternetConnection(context)
            }
        )

    suspend fun getNoteById(noteId: String) = noteDao.getNoteById(noteId)

    fun observeByNoteId(noteId: String)=noteDao.observeNoteById(noteId)

    suspend fun login(email:String,password:String)= withContext(Dispatchers.IO){
        try {
            val response=noteApi.login(AccountRequest(email, password))
            if(response.isSuccessful&&response.body()!!.successful){
                Resource.Success(response.body()?.message)
            }else{
                Resource.Error(  null,response.body()?.message?:  response.message())
            }
        }catch (e:Exception){
            Resource.Error(null,
                "Could not connect to the server.Please check your internet connection")
        }
    }

    suspend fun register(email:String,password:String)= withContext(Dispatchers.IO){
        try {
            val response=noteApi.register(AccountRequest(email, password))
            if(response.isSuccessful&&response.body()!!.successful){
                Resource.Success(response.body()?.message)
            }else{
                Resource.Error(  null, response.body()?.message?: response.message())
            }
        }catch (e:Exception){
            Resource.Error(null,
            "Could not connect to the server.Please check your internet connection")
        }
    }

    suspend fun addOwnerToNote(owner:String,noteId: String)= withContext(Dispatchers.IO){
        try {
            val response=noteApi.addOwnerToNote(AddOwnerRequest(owner, noteId))
            if(response.isSuccessful&&response.body()!!.successful){
                Resource.Success(response.body()?.message)
            }else{
                Resource.Error(  null,response.body()?.message?:  response.message())
            }
        }catch (e:Exception){
            Resource.Error(null,
                "Could not connect to the server.Please check your internet connection")
        }
    }

}