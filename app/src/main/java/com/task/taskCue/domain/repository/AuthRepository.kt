package com.task.taskCue.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.task.taskCue.utils.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun googleSignIn(idToken:String): Flow<AuthResult>
    fun getCurrentUser(): FirebaseUser?
    suspend fun signOut()
}