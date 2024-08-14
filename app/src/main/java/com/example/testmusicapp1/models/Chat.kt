package com.example.testmusicapp1.models

import java.security.Timestamp

data class Chat(
    val msg: String = "",
    val timeStamp: Timestamp? = null,
    val senderId: String = ""
)