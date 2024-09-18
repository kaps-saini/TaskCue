package com.task.taskCue.utils

import com.google.firebase.auth.FirebaseUser

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: FirebaseUser?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
