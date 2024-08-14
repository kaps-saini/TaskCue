package com.example.testmusicapp1.models

import java.security.Timestamp

data class Room(
    val id: String = "",
    val listOfUserIds: List<String> = emptyList(),
    val lastMsg: String = "",
    val lastMsgTimeStamp: Timestamp? = null,
    val lastSenderId: String = ""
)