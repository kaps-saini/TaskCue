package com.task.taskCue.di

import com.task.taskCue.data.remote.RepositoryImpl
import com.task.taskCue.domain.repository.TaskRepository
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
    ): TaskRepository




}