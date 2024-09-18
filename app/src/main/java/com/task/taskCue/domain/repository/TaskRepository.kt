package com.task.taskCue.domain.repository

import androidx.lifecycle.LiveData
import com.task.taskCue.domain.models.TaskModel
import com.task.taskCue.domain.models.UserDataModel
import com.task.taskCue.utils.TaskResult

interface TaskRepository {

    suspend fun addTask(title:String,desc:String):TaskResult<String>
    suspend fun getTask():TaskResult<MutableList<TaskModel>>
    suspend fun delete(taskId:String):TaskResult<String>
    suspend fun update(taskId: String,title:String,desc:String,status:Boolean):TaskResult<String>
    suspend fun currentUserData():TaskResult<UserDataModel>
    suspend fun getTaskLiveData():LiveData<TaskResult<List<TaskModel>>>
}