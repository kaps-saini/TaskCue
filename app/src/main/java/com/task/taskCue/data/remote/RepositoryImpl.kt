package com.task.taskCue.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.task.taskCue.domain.models.TaskModel
import com.task.taskCue.domain.models.UserDataModel
import com.task.taskCue.domain.repository.TaskRepository
import com.task.taskCue.utils.TaskResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RepositoryImpl @Inject constructor() : TaskRepository {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid.toString()
    private val db = FirebaseFirestore.getInstance()
    private val docRef = db.collection("users").document(userId)

    override suspend fun addTask(title: String, desc: String): TaskResult<String> {
        return try {
            val documentId = docRef.collection("task").document().id
            val taskModel = TaskModel(documentId, title, desc, false)
            val ref = docRef.collection("task").document(documentId).set(taskModel).await()
            TaskResult.Success("Successfully added")
        } catch (e: Exception) {
            TaskResult.Error(e.message.toString())
        }
    }

    override suspend fun getTask(): TaskResult<MutableList<TaskModel>> {
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
                TaskResult.Success(taskList)
            } else {
                TaskResult.Error("No data")
            }
        } catch (e: Exception) {
            TaskResult.Error(e.message.toString())
        }
    }

    override suspend fun delete(taskId: String): TaskResult<String> {
        return try {
            docRef.collection("task").document(taskId).delete().await()
            TaskResult.Success("Successfully Deleted")
        } catch (e: Exception) {
            TaskResult.Error(e.message.toString())
        }
    }

    override suspend fun update(
        taskId: String,
        title: String,
        desc: String,
        status: Boolean
    ): TaskResult<String> {
        return try {
            val taskModel = TaskModel(taskId, title, desc, status)
            val ref = docRef.collection("task").document(taskId).set(taskModel).await()
            TaskResult.Success("Successfully updated")
        } catch (e: Exception) {
            TaskResult.Error(e.message.toString())
        }
    }

    override suspend fun currentUserData(): TaskResult<UserDataModel> {
        return try {
            // Fetch the document snapshot using coroutine `await` for suspending function
            val documentSnapshot = db.collection("users").document(userId).get().await()

            // Check if the document exists and is not empty
            if (documentSnapshot.exists()) {
                // Convert the document snapshot to UserDataModel
                val userData = documentSnapshot.toObject<UserDataModel>()

                // Check if the data was successfully converted
                if (userData != null) {
                    TaskResult.Success(userData)
                } else {
                    TaskResult.Error("No data available")
                }
            } else {
                TaskResult.Error("Document does not exist")
            }
        } catch (e: Exception) {
            TaskResult.Error(e.message ?: "An error occurred")
        }
    }

    override suspend fun getTaskLiveData(): LiveData<TaskResult<List<TaskModel>>> {
        val liveData = MutableLiveData<TaskResult<List<TaskModel>>>()

        // Set up the real-time listener
        docRef.collection("task").addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Use a default message if e.message is null
                liveData.value = TaskResult.Error(e.message ?: "An unknown error occurred")
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val taskList = mutableListOf<TaskModel>()
                for (document in snapshot.documents) {
                    val task = document.toObject(TaskModel::class.java)
                    if (task != null) {
                        taskList.add(task)
                    }
                }
                liveData.value = TaskResult.Success(taskList)
            } else {
                liveData.value = TaskResult.Error("No data")
            }
        }

        return liveData
    }
}