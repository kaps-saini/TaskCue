package com.task.taskCue.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.task.taskCue.R
import com.task.taskCue.domain.models.TaskModel
import com.google.android.material.divider.MaterialDivider

class TaskAdapter:RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view:View):RecyclerView.ViewHolder(view){
        private val title:TextView = view.findViewById(R.id.tvTaskName)
       // private val taskDescription:TextView = view.findViewById(R.id.tvTaskDescription)
        private val ivOption:ImageView = view.findViewById(R.id.ivOption)
        val cardView :CardView = view.findViewById<CardView>(R.id.cvTask)
        val divider:MaterialDivider = view.findViewById(R.id.divider)

        fun bind(taskModel: TaskModel){
            title.text = taskModel.taskTitle
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, dataItem:Long, taskModel: TaskModel)
    }

    private var onItemClickListener:OnClickListener? = null

    fun setOnItemClickListener(listener:OnClickListener){
        onItemClickListener = listener
    }

    private val diffUtil = object : DiffUtil.ItemCallback<TaskModel>(){
        override fun areItemsTheSame(oldItem: TaskModel, newItem: TaskModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskModel, newItem: TaskModel): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.single_task_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = differ.currentList[position]
        holder.bind(currentTask)
        if (currentTask.taskCompleted){
            holder.cardView.alpha = 0.4f
            holder.divider.visibility = View.VISIBLE
        }else{
            holder.cardView.alpha = 1.0f
            holder.divider.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{
            onItemClickListener?.onClick(position,getItemId(position),currentTask)
        }
    }
}