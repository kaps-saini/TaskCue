package com.example.testmusicapp1.models

import java.io.Serializable

data class UserDataModel(
    val userId: String = "",
    val userName:String = "",
    val mobileNo: String = "",
    val userImage: String? = "",
    val gender:String = ""
): Serializable