package com.example.testmusicapp1.di

import com.example.testmusicapp1.firestoredb.RepositoryImpl
import com.example.testmusicapp1.firestoredb.TaskRepository
import com.example.testmusicapp1.repository.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideRepositoryInstance(
        repositoryImpl: RepositoryImpl
    ):Repository


}