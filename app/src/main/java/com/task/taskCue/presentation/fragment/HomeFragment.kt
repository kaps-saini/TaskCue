package com.task.taskCue.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.task.taskCue.R
import com.task.taskCue.adapter.TaskAdapter
import com.task.taskCue.databinding.FragmentHomeBinding
import com.task.taskCue.helper.Helper
import com.task.taskCue.domain.models.TaskModel
import com.task.taskCue.presentation.bottomsheet.TaskDetailSheet
import com.task.taskCue.presentation.vm.TaskViewModel
import com.task.taskCue.utils.TaskResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private val viewModel by viewModels<TaskViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        setupRecyclerView()

        binding.fab.setOnClickListener{
            val model = TaskModel(null.toString(),"","")
            val action = HomeFragmentDirections.actionHomeFragmentToTaskCreator(model)
            findNavController().navigate(action)
        }

        viewModel.getTasks()
        viewModel.tasks.observe(viewLifecycleOwner){ response->
            when(response){
                is TaskResult.Error -> {
                    if (response.message.equals("No data",true)){
                        binding.tvNoTaskMsg.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }else{
                        val errorMessage = response.message ?: "An unknown error occurred"
                        Helper.makeSnackBar(requireView(), errorMessage)
                    }
                }
                is TaskResult.Loading -> Helper.makeSnackBar(requireView(), "Loading")
                is TaskResult.Success -> {
                    taskAdapter.differ.submitList(response.data.toList())
                    binding.tvNoTaskMsg.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }

        taskAdapter.setOnItemClickListener(object :TaskAdapter.OnClickListener{
            override fun onClick(position: Int, dataItem: Long, taskModel: TaskModel) {
                openBottomTaskDetailSheet(taskModel.id,taskModel.taskTitle,taskModel.taskDescription,taskModel.taskCompleted)
            }
        })

        return binding.root
    }

    private fun openBottomTaskDetailSheet(taskId:String,taskTitle: String, taskDescription: String?,taskStatus:Boolean) {
        val sheet =
            TaskDetailSheet.newInstance(taskId, taskTitle, taskDescription ?: "", taskStatus)
        sheet.show(requireActivity().supportFragmentManager,"Bottom Task Detail sheet")
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun setupRecyclerView(){
        taskAdapter = TaskAdapter()
        binding.recyclerView.apply {
            this.adapter = taskAdapter
            this.layoutManager = LinearLayoutManager(requireContext())
        }
    }



}