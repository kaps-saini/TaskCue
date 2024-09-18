package com.task.taskCue.utils

sealed class TaskResult<T>{

    data class Success<T>(val data:T):TaskResult<T>()
    class Error<T>(val message:String?,val data: T?= null):TaskResult<T>()
    class Loading<T>:TaskResult<T>()
}