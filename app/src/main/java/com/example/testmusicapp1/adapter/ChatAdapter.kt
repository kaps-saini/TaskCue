package com.example.testmusicapp1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.testmusicapp1.R
import com.example.testmusicapp1.models.Chat
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val chats: List<Chat>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val lastMsg: TextView = itemView.findViewById(R.id.tvLastMsg)

        fun bind(chat: Chat){

        }
    }

    private val diffUtil = object: DiffUtil.ItemCallback<Chat>(){
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.senderId == newItem.senderId
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_chat_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profiles = differ.currentList[position]

    }

}