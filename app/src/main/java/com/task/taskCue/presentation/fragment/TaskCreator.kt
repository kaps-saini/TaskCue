package com.task.taskCue.presentation.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.task.taskCue.R
import com.task.taskCue.databinding.FragmentTaskCreatorBinding
import com.task.taskCue.helper.Helper
import com.task.taskCue.helper.OfflineNotify
import com.task.taskCue.presentation.vm.TaskViewModel
import com.task.taskCue.utils.TaskResult
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class TaskCreator : Fragment() {

    private var _binding:FragmentTaskCreatorBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<TaskViewModel>()
    private val navArgs by navArgs<TaskCreatorArgs>()
    private lateinit var taskTitle:String
    private var taskDesc:String? = null
    private var time:Calendar? = null

      @SuppressLint("SuspiciousIndentation")
      override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_task_creator, container, false)

          binding.etTitle.addTextChangedListener(object : TextWatcher {
              override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
              override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { buttonVisibility() }
              override fun afterTextChanged(s: Editable?) {}
          })

          fillData()

//          // In your Activity or Fragment
//          val timerOptions = listOf("1 hour", "2 hours", "3 hours", "4 hours", "5 hours", "6 hours")
//          val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, timerOptions)
//          binding.autoCompleteTextView.setAdapter(adapter)
//
//          binding.autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
//              binding.autoCompleteTextView.showDropDown()
//              val selectedTimer = parent.getItemAtPosition(position) as String
//              hours = selectedTimer.split(" ")[0].toInt()
//          }

          binding.timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
              val calendar = Calendar.getInstance().apply {
                  set(Calendar.HOUR_OF_DAY, hourOfDay)
                  set(Calendar.MINUTE, minute)
                  set(Calendar.SECOND, 0)
                  set(Calendar.MILLISECOND, 0)

                  // If the selected time is before the current time, set the alarm for the next day
                  if (timeInMillis < System.currentTimeMillis()) {
                      add(Calendar.DAY_OF_YEAR, 1)
                  }
              }
              time = calendar
          }

          binding.btnAdd.setOnClickListener {
              addTask()
              if (time != null) {
                  checkAlarmPermission()
                  try {
                      scheduleNotification(time!!)
                  }catch (e:Exception){
                      Helper.makeSnackBar(requireView(),e.message.toString())
                  }
              }
          }

          viewModel.taskStatus.observe(viewLifecycleOwner){ response->
             Snackbar.make(requireView(),response.toString(),Snackbar.LENGTH_SHORT).show()
              when(response){
                  is TaskResult.Error -> Helper.makeSnackBar(requireView(),response.toString())
                  is TaskResult.Loading -> Helper.makeSnackBar(requireView(),"Loading")
                  is TaskResult.Success -> {
                      Helper.makeSnackBar(requireView(),response.data)
                      findNavController().navigateUp()
                  }
              }
          }

          return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun addTask(){
        taskTitle = binding.etTitle.text.toString()
        taskDesc = binding.etDescription.text.toString()
        if (taskTitle.isNotEmpty()){
            viewModel.addTask(taskTitle,taskDesc?:"")
        }
    }

//    private fun updateTask(){
//        taskTitle = binding.etTitle.text.toString()
//        taskDesc = binding.etDescription.text.toString()
//        val id = navArgs.taskModel.id
//        if (taskTitle.isNotEmpty()){
//            viewModel.updateTask(id,taskTitle,taskDesc?:"",false)
//        }
//    }

    private fun fillData(){
        if (navArgs.taskModel.id.isNotEmpty()){
            binding.etTitle.setText(navArgs.taskModel.taskTitle)
            binding.etDescription.setText(navArgs.taskModel.taskDescription)
        }
    }

    private fun buttonVisibility(){
        Helper.btnVisibility(requireContext(),binding.etTitle,button = binding.btnAdd)
    }

    private fun scheduleNotification(time: Calendar) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, OfflineNotify::class.java)

        val title = binding.etTitle.text.toString()
        val desc = binding.etDescription.text.toString()
        intent.putExtra("title", title)
        intent.putExtra("desc", desc)

        val requestCode = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = time.timeInMillis
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

//        alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
//            PendingIntent.getBroadcast(context, 0, intent, 0)
//        }
//
//        alarmMgr?.set(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + 60 * 1000,
//            alarmIntent
//        )

        Helper.makeSnackBar(requireView(),"Reminder Set")

        findNavController().navigateUp()
    }

    private val REQUEST_CODE_SCHEDULE_EXACT_ALARM = 1

    private fun checkAlarmPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                val notify = OfflineNotify()
                notify.requestNotificationPermission(requireActivity())

                val alarmManager = ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)
                if (alarmManager?.canScheduleExactAlarms() == false) {
                    Intent().also { intent ->
                        intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM)
                    }
                }
            }
        }
    }



}