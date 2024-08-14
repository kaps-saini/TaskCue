package com.example.testmusicapp1.firestoredb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testmusicapp1.models.TaskModel
import com.example.testmusicapp1.utils.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TaskRepository {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid.toString()
    private val db = FirebaseFirestore.getInstance()
    private val docRef = db.collection("users")

    fun getTasksLiveData(): LiveData<Resources<List<TaskModel>>> {
        val liveData = MutableLiveData<Resources<List<TaskModel>>>()

        // Set up the real-time listener
        docRef.document(userId).collection("task").addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Use a default message if e.message is null
                liveData.value = Resources.Error(e.message ?: "An unknown error occurred")
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
                liveData.value = Resources.Success(taskList)
            } else {
                liveData.value = Resources.Error("No data")
            }
        }

        return liveData
    }
}