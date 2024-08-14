package com.example.testmusicapp1.di

import com.example.testmusicapp1.firestoredb.ChatRepository
import com.example.testmusicapp1.firestoredb.ProfileRepository
import com.example.testmusicapp1.firestoredb.TaskRepository
import com.example.testmusicapp1.utils.NetworkConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkInstance():NetworkConnection{
        return NetworkConnection()
    }

    @Provides
    @Singleton
    fun provideRepository():TaskRepository{
        return TaskRepository()
    }

    @Provides
    @Singleton
    fun provideChatRepository():ChatRepository{
        return ChatRepository()
    }

    @Provides
    @Singleton
    fun provideProfileRepository():ProfileRepository{
        return ProfileRepository()
    }
}