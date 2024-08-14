package com.example.testmusicapp1.utils

sealed class Resources<T>{

    data class Success<T>(val data:T):Resources<T>()
    class Error<T>(val message:String?,val data: T?= null):Resources<T>()
    class Loading<T>:Resources<T>()
}