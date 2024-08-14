package com.example.testmusicapp1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.example.testmusicapp1.R
import com.example.testmusicapp1.models.Chat
import com.google.firebase.auth.FirebaseAuth

class MessagingAdapter(private val messages: List<Chat>) : RecyclerView.Adapter<MessagingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val otherUserMsg: TextView = itemView.findViewById(R.id.tvOtherUserMsg)
        val otherUserTime: TextView = itemView.findViewById(R.id.tvOtherUserTime)
        val currentUserMsg: TextView = itemView.findViewById(R.id.tvCurrentUserMsg)
        val currentUserTime: TextView = itemView.findViewById(R.id.tvCurrentUserTime)
        val otherUserMsgLayout: LinearLayout = itemView.findViewById(R.id.otherUserMsgLayout)
        val currentUserMsgLayout: LinearLayout = itemView.findViewById(R.id.currentUserMsgLayout)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_message, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = differ.currentList[position]
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser?.uid == message.senderId) {
            holder.otherUserMsgLayout.visibility = View.GONE
            holder.currentUserMsgLayout.visibility = View.VISIBLE
            holder.currentUserMsg.text = message.msg
            holder.currentUserTime.text = message.timeStamp.toString()
        }else{
            holder.otherUserMsgLayout.visibility = View.VISIBLE
            holder.currentUserMsgLayout.visibility = View.GONE
            holder.otherUserMsg.text = message.msg
            holder.otherUserTime.text = message.timeStamp.toString()
        }
    }

}
