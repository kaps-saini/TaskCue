package com.task.taskCue.di

import com.google.firebase.auth.FirebaseAuth
import com.task.taskCue.data.remote.AuthRepositoryImpl
import com.task.taskCue.data.remote.ProfileRepository
import com.task.taskCue.data.remote.TaskRepository
import com.task.taskCue.domain.repository.AuthRepository
import com.task.taskCue.utils.NetworkConnection
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
    fun provideRepository(): TaskRepository {
        return TaskRepository()
    }

    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository {
        return ProfileRepository()
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository =
        AuthRepositoryImpl(firebaseAuth)

//    @Provides
//    fun provideAuthUseCase(authRepository: AuthRepository): AuthUseCase =
//        AuthUseCaseImpl(authRepository)
}