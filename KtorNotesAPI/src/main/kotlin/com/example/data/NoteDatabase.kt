@file:Suppress("NAME_SHADOWING")

package com.example.data

import com.example.data.collections.Note
import com.example.data.collections.User
import com.example.security.checkForHashPassword
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

private val client= KMongo.createClient().coroutine
private val database= client.getDatabase("NotesDataBase")
private val users= database.getCollection<User>()
private val notes= database.getCollection<Note>()

suspend fun registerUser(user: User)=
    users.insertOne(user).wasAcknowledged()

suspend fun checkIfUsersExists(email:String)=
    users.findOne(User::email eq email)!= null

suspend fun checkPasswordForEmail(email: String,passwordToCheck:String):Boolean{
    val actualPassword= users.findOne(User::email eq email)?.password?:return false
    return checkForHashPassword(passwordToCheck,actualPassword)
}

suspend fun getNotesForUsers(email: String)=
    notes.find(Note::owners contains email).toList()

suspend fun saveNote(note: Note):Boolean{
    val notesExist=notes.findOneById(note.id)!=null
    return if(notesExist){
        notes.updateOneById(note.id,note).wasAcknowledged()
    }else{
        notes.insertOne(note).wasAcknowledged()
    }
}

suspend fun deleteNoteForUser(email: String, noteID: String): Boolean {
    val note = notes.findOne(Note::id eq noteID, Note::owners contains email)
    note?.let { note ->
        if(note.owners.size > 1) {
            // the note has multiple owners, so we just delete the email from the owners list
            val newOwners = note.owners - email
            val updateResult = notes.updateOne(Note::id eq note.id, setValue(Note::owners, newOwners))
            return updateResult.wasAcknowledged()
        }
        return notes.deleteOneById(note.id).wasAcknowledged()
    } ?: return false
}

suspend fun addOwnersToNote(noteId:String,owner:String):Boolean{
    val owners= notes.findOneById(noteId)?.owners?:return false
    return notes.updateOne(Note::id eq noteId, setValue(Note::owners,owners+owner)).wasAcknowledged()
}

suspend fun isOwnerOfNote(noteId: String,owner: String):Boolean{
    val note= notes.findOneById(Note::id eq noteId)?.owners?:return false
    return owner in note
}

