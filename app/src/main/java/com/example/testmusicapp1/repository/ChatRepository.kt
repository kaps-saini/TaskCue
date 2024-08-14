package com.example.testmusicapp1.repository

interface ChatRepository {
    suspend fun getChats()
    suspend fun sendMessage()
}