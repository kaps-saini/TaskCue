package com.task.taskCue.data.remote

import android.util.Log
import com.task.taskCue.domain.models.UserDataModel
import com.task.taskCue.utils.TaskResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    var result = ""

    suspend fun setupProfile(userId: String, profileData: UserDataModel): TaskResult<String> {
        return try {
            usersCollection.document(userId).set(profileData).await()
            TaskResult.Success("Profile set up successfully")
        } catch (e: Exception) {
            // Log the exception and provide a more descriptive error message
            Log.e("FirestoreError", "Error setting up profile", e)
            TaskResult.Error("Failed to set up profile: ${e.message ?: "Unknown error"}")
        }
    }

}