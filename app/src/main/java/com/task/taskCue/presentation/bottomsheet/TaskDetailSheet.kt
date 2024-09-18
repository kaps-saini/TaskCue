package com.task.taskCue.presentation.bottomsheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.task.taskCue.R
import com.task.taskCue.databinding.TaskReaderSheetBinding
import com.task.taskCue.helper.Helper
import com.task.taskCue.domain.models.TaskModel
import com.task.taskCue.presentation.fragment.HomeFragmentDirections
import com.task.taskCue.presentation.vm.TaskViewModel
import com.task.taskCue.utils.TaskResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailSheet : BottomSheetDialogFragment() {

    private var _binding: TaskReaderSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<TaskViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.task_reader_sheet, container, false)

        val taskId = arguments?.getString("task_id")
        val title = arguments?.getString("task_title")
        val desc = arguments?.getString("task_description")
        val isTaskComplete = arguments?.getBoolean("task_status")

        if (isTaskComplete == true){
            binding.btnDone.text = "Mark as incomplete"
        }else{
            binding.btnDone.text = "Mark as complete"
        }

        binding.tvName.text = title.toString()
        binding.tvTaskDescription.text = desc.toString()

        binding.btnEdit.setOnClickListener {
            val model = TaskModel(taskId!!,title!!,desc)
            val action = HomeFragmentDirections.actionHomeFragmentToTaskCreator(model)
            findNavController().navigate(action)
            this.dismissNow()
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Helper.shareText(title!!,desc)
            val chooser = Intent.createChooser(shareIntent,"Share via")
            startActivity(chooser)
        }

        binding.btnDelete.setOnClickListener {
            if (taskId != null) {
                viewModel.deleteTask(taskId)
            }
        }

        binding.btnDone.setOnClickListener {
            if (isTaskComplete == true){
                if (taskId != null) {
                    viewModel.updateTask(taskId,title!!,desc!!,false)
                    viewModel.getTasks()
                }
            }else{
                if (taskId != null) {
                    viewModel.updateTask(taskId,title!!,desc!!,true)
                    viewModel.getTasks()
                }
            }
            this.dismissNow()
        }

        viewModel.taskStatus.observe(viewLifecycleOwner){ response ->
            when(response){
                is TaskResult.Error -> Helper.makeSnackBar(requireView(),response.toString())
                is TaskResult.Loading -> {}
                is TaskResult.Success -> {
                    Helper.makeSnackBar(requireView(),response.toString())
                    this.dismissNow()
                }
            }

        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    companion object {
        fun newInstance(taskId: String, taskTitle: String, taskDescription: String,taskStatus:Boolean): TaskDetailSheet {
            val args = Bundle()
            args.putString("task_id", taskId)
            args.putString("task_title", taskTitle)
            args.putString("task_description", taskDescription)
            args.putBoolean("task_status",taskStatus)

            val fragment = TaskDetailSheet()
            fragment.arguments = args
            return fragment
        }
    }

}