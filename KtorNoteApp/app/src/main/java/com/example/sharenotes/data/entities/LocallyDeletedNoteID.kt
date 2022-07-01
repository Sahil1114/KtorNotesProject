package com.example.sharenotes.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locally_deleted_note_id")
data class LocallyDeletedNoteID(
    @PrimaryKey(autoGenerate = false)
    val deletedNoteID:String
)
