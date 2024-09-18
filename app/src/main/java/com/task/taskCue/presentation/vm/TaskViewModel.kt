package com.task.taskCue.presentation.vm

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.taskCue.data.remote.ProfileRepository
import com.task.taskCue.domain.models.TaskModel
import com.task.taskCue.domain.models.UserDataModel
import com.task.taskCue.domain.repository.AuthRepository
import com.task.taskCue.domain.repository.TaskRepository
import com.task.taskCue.utils.AuthResult
import com.task.taskCue.utils.TaskResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    val app:Application
):ViewModel() {

    private val _taskResponse:MutableLiveData<TaskResult<String>> = MutableLiveData()
    val taskResponse:LiveData<TaskResult<String>> get() = _taskResponse

    private var _tasks:LiveData<TaskResult<List<TaskModel>>> = MutableLiveData()
    val tasks: LiveData<TaskResult<List<TaskModel>>>  get() = _tasks

    private val _taskStatus:MutableLiveData<TaskResult<String>> = MutableLiveData()
    val taskStatus:LiveData<TaskResult<String>> get() = _taskStatus

    private val _profileData:MutableLiveData<TaskResult<UserDataModel>> = MutableLiveData()
    val profileData:LiveData<TaskResult<UserDataModel>> get() = _profileData

    private val _profileStatus:MutableLiveData<TaskResult<String>> = MutableLiveData()
    val profileStatus:LiveData<TaskResult<String>> get() = _profileStatus

    private val _authStatus:MutableStateFlow<AuthResult> = MutableStateFlow(AuthResult.Loading)
    val authStatus:Flow<AuthResult> get() = _authStatus

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authRepository.googleSignIn(idToken)
                .collect { result ->
                    _authStatus.value = result
                }
        }
    }

    fun addTask(title:String,desc:String) = viewModelScope.launch {
            val response = repository.addTask(title,desc)
            _taskStatus.value = response
    }

    fun updateTask(taskId: String,title: String,desc: String,status:Boolean) = viewModelScope.launch {
            val response = repository.update(taskId,title,desc,status)
            _taskResponse.value = response
    }

    fun getProfileData() = viewModelScope.launch {
        val response = repository.currentUserData()
        _profileData.value = response
    }

    fun deleteTask(taskId:String) = viewModelScope.launch {
        _taskStatus.value = TaskResult.Loading()
        val response = repository.delete(taskId)
        _taskStatus.value = response
    }

    fun getTasks() = viewModelScope.launch{
        _tasks = repository.getTaskLiveData()
    }
//
//    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
//    val rooms: StateFlow<List<Room>> get() = _rooms
//
//    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
//    val chats: StateFlow<List<Chat>> get() = _chats
//
//    fun profileUpdate(userId: String,userData: UserDataModel){
//        _profileStatus.value = TaskResult.Loading()
//        viewModelScope.launch(Dispatchers.IO) {
//            val result = profileRepository.setupProfile(userId,userData)
//            withContext(Dispatchers.Main){
//                _profileStatus.value = result
//            }
//        }
//    }

}