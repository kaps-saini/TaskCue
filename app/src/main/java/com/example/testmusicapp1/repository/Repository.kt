package com.example.testmusicapp1.repository

import com.example.testmusicapp1.models.TaskModel
import com.example.testmusicapp1.models.UserDataModel
import com.example.testmusicapp1.utils.Resources

interface Repository {

    suspend fun addTask(title:String,desc:String):Resources<String>
    suspend fun getTask():Resources<MutableList<TaskModel>>
    suspend fun delete(taskId:String):Resources<String>
    suspend fun update(taskId: String,title:String,desc:String,status:Boolean):Resources<String>
    suspend fun currentUserData():Resources<UserDataModel>
}