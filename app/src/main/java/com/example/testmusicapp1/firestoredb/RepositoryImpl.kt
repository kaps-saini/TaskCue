package com.example.testmusicapp1.firestoredb

import com.example.testmusicapp1.models.TaskModel
import com.example.testmusicapp1.models.UserDataModel
import com.example.testmusicapp1.repository.Repository
import com.example.testmusicapp1.utils.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RepositoryImpl @Inject constructor():Repository {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid.toString()
    private val db = FirebaseFirestore.getInstance()
    private val docRef = db.collection("users").document(userId)

    override suspend fun addTask(title:String,desc:String): Resources<String> {
           return try{
               val documentId = docRef.collection("task").document().id
               val taskModel = TaskModel(documentId,title,desc,false)
               val ref = docRef.collection("task").document(documentId).set(taskModel).await()
                Resources.Success("Successfully added")
           }catch (e:Exception){
                Resources.Error(e.message.toString())
           }
    }

    override suspend fun getTask(): Resources<MutableList<TaskModel>> {
        return try {
            val querySnapshot = docRef.collection("task").get().await()
            if (!querySnapshot.isEmpty) {
                val taskList = mutableListOf<TaskModel>()
                for (document in querySnapshot.documents) {
                    val task = document.toObject(TaskModel::class.java)
                    if (task != null) {
                        taskList.add(task)
                    }
                }
                Resources.Success(taskList)
            } else {
                Resources.Error("No data")
            }
        } catch (e: Exception) {
            Resources.Error(e.message.toString())
        }
    }

    override suspend fun delete(taskId: String): Resources<String> {
        return try {
            docRef.collection("task").document(taskId).delete().await()
            Resources.Success("Successfully Deleted")
        }catch (e:Exception){
            Resources.Error(e.message.toString())
        }
    }

    override suspend fun update(taskId: String,title:String,desc:String,status:Boolean): Resources<String> {
        return try{
            val taskModel = TaskModel(taskId,title,desc,status)
            val ref = docRef.collection("task").document(taskId).set(taskModel).await()
            Resources.Success("Successfully updated")
        }catch (e:Exception){
            Resources.Error(e.message.toString())
        }
    }

    override suspend fun currentUserData(): Resources<UserDataModel> {
        return try {
            // Fetch the document snapshot using coroutine `await` for suspending function
            val documentSnapshot = db.collection("users").document(userId).get().await()

            // Check if the document exists and is not empty
            if (documentSnapshot.exists()) {
                // Convert the document snapshot to UserDataModel
                val userData = documentSnapshot.toObject<UserDataModel>()

                // Check if the data was successfully converted
                if (userData != null) {
                    Resources.Success(userData)
                } else {
                    Resources.Error("No data available")
                }
            } else {
                Resources.Error("Document does not exist")
            }
        } catch (e: Exception) {
            Resources.Error(e.message ?: "An error occurred")
        }
    }



}