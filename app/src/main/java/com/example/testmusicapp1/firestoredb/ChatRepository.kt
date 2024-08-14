package com.example.testmusicapp1.firestoredb

import com.example.testmusicapp1.models.Chat
import com.example.testmusicapp1.models.Room
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val roomsCollection = db.collection("rooms")

    suspend fun getRoomsForUser(userId: String): List<Room> {
        return try {
            val snapshot = roomsCollection.whereArrayContains("listOfUserIds", userId).get().await()
            snapshot.toObjects(Room::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getChatsForRoom(roomId: String): List<Chat> {
        return try {
            val snapshot = roomsCollection.document(roomId).collection("chats").get().await()
            snapshot.toObjects(Chat::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendMessage(roomId: String, chat: Chat) {
        try {
            roomsCollection.document(roomId).collection("chats").add(chat).await()
            roomsCollection.document(roomId).update(
                mapOf(
                    "lastMsg" to chat.msg,
                    "lastMsgTimeStamp" to chat.timeStamp,
                    "lastSenderId" to chat.senderId
                )
            ).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
}
