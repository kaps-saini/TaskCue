package com.task.taskCue.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.task.taskCue.domain.models.TaskModel
import com.task.taskCue.utils.TaskResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TaskRepository {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid.toString()
    private val db = FirebaseFirestore.getInstance()
    private val docRef = db.collection("users")

    fun getTasksLiveData(): LiveData<TaskResult<List<TaskModel>>> {
        val liveData = MutableLiveData<TaskResult<List<TaskModel>>>()

        // Set up the real-time listener
        docRef.document(userId).collection("task").addSnapshotListener { snapshot, e ->
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