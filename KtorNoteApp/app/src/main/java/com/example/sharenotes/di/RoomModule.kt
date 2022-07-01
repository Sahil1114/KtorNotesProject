package com.example.sharenotes.di

import android.content.Context
import androidx.room.Room
import com.example.sharenotes.data.local.NoteDao
import com.example.sharenotes.data.local.NotesDatabase
import com.example.sharenotes.utils.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    )=Room.databaseBuilder(
        context,
        NotesDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideNoteDao(
        db:NotesDatabase
    ):NoteDao=db.noteDao()
}