package com.task.taskCue.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.task.taskCue.domain.models.UserDataModel
import com.task.taskCue.domain.repository.AuthRepository
import com.task.taskCue.utils.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl (
    private val auth: FirebaseAuth,
): AuthRepository {

        override suspend fun googleSignIn(idToken: String): Flow<AuthResult>  = flow{
            emit(AuthResult.Loading)

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            try {
                val authResult = auth.signInWithCredential(credential).await()
                emit(AuthResult.Success(authResult.user))
                addUserToFirebase(auth.currentUser?.uid.toString())
            } catch (e: Exception) {
                emit(AuthResult.Error(e.localizedMessage ?: "Unknown error occurred"))
            }
        }.catch { e ->
        emit(AuthResult.Error(e.localizedMessage ?: "Unknown error occurred"))
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun signOut() = auth.signOut()

    override fun addUserToFirebase(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userCollectionRef = db.collection("users")
        val userData = UserDataModel(userId,
            auth.currentUser?.displayName ?: "",
            auth.currentUser?.email ?: "",
            auth.currentUser?.photoUrl.toString(),
            null)
        userCollectionRef.document(userId).set(userData)
    }

}