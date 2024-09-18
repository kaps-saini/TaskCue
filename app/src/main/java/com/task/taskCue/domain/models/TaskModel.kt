package com.task.taskCue.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskModel(
    val id:String = "",
    val taskTitle:String = "",
    val taskDescription: String? = "",
    val taskCompleted:Boolean = false
):Parcelable