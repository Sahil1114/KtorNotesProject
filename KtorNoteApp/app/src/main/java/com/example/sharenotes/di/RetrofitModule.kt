package com.example.sharenotes.di

import com.example.sharenotes.data.remote.BasicAuthInterceptor
import com.example.sharenotes.data.remote.NoteApi
import com.example.sharenotes.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideBasicAuthInterceptor()=BasicAuthInterceptor()

    @Provides
    @Singleton
    fun provideNoteApi(
        basicAuthInterceptor: BasicAuthInterceptor
    ):NoteApi{
        val client=OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .build()
        return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NoteApi::class.java)
    }
}