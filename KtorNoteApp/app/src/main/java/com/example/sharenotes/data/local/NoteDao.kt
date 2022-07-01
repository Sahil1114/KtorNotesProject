package com.example.sharenotes.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sharenotes.data.entities.LocallyDeletedNoteID
import com.example.sharenotes.data.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteID")
    suspend fun deleteNoteById(noteID: String)

    @Query("DELETE FROM notes WHERE isSynced = 1")
    suspend fun deleteAllSyncedNotes()

    @Query("SELECT * FROM notes WHERE id = :noteID")
    fun observeNoteById(noteID: String): LiveData<Note>

    @Query("SELECT * FROM notes WHERE id = :noteID")
    suspend fun getNoteById(noteID: String): Note?

    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getAllUnsyncedNotes(): List<Note>

    @Query("SELECT * FROM locally_deleted_note_id")
    suspend fun getAllLocallyDeletedNoteIDs():List<LocallyDeletedNoteID>

    @Query("DELETE FROM locally_deleted_note_id WHERE deletedNoteID=:deletedNoteID")
    suspend fun deleteLocallyNoteId(deletedNoteID: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insetLocallyDeletedNoteID(locallyDeletedNoteID: LocallyDeletedNoteID)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
