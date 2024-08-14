package com.example.testmusicapp1.firestoredb

import android.util.Log
import com.example.testmusicapp1.models.UserDataModel
import com.example.testmusicapp1.utils.Resources
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    var result = ""

    suspend fun setupProfile(userId: String, profileData: UserDataModel): Resources<String> {
        return try {
            usersCollection.document(userId).set(profileData).await()
            Resources.Success("Profile set up successfully")
        } catch (e: Exception) {
            // Log the exception and provide a more descriptive error message
            Log.e("FirestoreError", "Error setting up profile", e)
            Resources.Error("Failed to set up profile: ${e.message ?: "Unknown error"}")
        }
    }

//    suspend fun getProfileData(userId: String):Resources<UserDataModel>{
//         return try {
//            val snapshot = usersCollection.document(userId).get().addOnCompleteListener {
//                if (it.isSuccessful){
//                    val profileData = it.result.toObject(UserDataModel::class.java)
//                    Resources.Success(profileData!!)
//                }
//            }
//                .addOnFailureListener {
//                    Resources.Error("Error")
//                }
//
//         }catch (e:Exception){
//             Resources.Error("Failed to fetch profile data")
//         }
//    }


}