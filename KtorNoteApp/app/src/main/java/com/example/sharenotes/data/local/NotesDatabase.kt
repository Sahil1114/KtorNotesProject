package com.example.sharenotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sharenotes.data.entities.LocallyDeletedNoteID
import com.example.sharenotes.data.entities.Note

@Database(
    entities = [Note::class,LocallyDeletedNoteID::class],
    version = 1
)
@TypeConverters(Convertors::class)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}