package com.task.taskCue.domain.models

import java.io.Serializable

data class GoogleResponseBody(
    val message: String,
    val status: Boolean,
    val data: ProfileData
)

data class ProfileData(
    val profile: UserDataModel,
    val token: Token
)

data class Token(
    val refreshToken: String,
    val accessToken: String
)

data class UserDataModel(
    val userId: String = "",
    val userName:String = "",
    val mobileNo: String = "",
    val userImage: String? = "",
    val gender:String = ""
): Serializable