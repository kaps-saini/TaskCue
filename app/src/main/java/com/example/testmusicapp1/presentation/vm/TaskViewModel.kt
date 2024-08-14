package com.example.testmusicapp1.presentation.vm

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testmusicapp1.firestoredb.ChatRepository
import com.example.testmusicapp1.firestoredb.ProfileRepository
import com.example.testmusicapp1.firestoredb.TaskRepository
import com.example.testmusicapp1.models.Chat
import com.example.testmusicapp1.models.Room
import com.example.testmusicapp1.models.TaskModel
import com.example.testmusicapp1.models.UserDataModel
import com.example.testmusicapp1.repository.Repository
import com.example.testmusicapp1.utils.NetworkConnection
import com.example.testmusicapp1.utils.Resources
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val repository: Repository,
    private val chatRepository:ChatRepository,
    private val profileRepository: ProfileRepository,
    val app:Application
):ViewModel() {

    private val _taskResponse:MutableLiveData<Resources<String>> = MutableLiveData()
    val taskResponse:LiveData<Resources<String>> get() = _taskResponse

    private var _tasks:LiveData<Resources<List<TaskModel>>> = MutableLiveData()
    val tasks: LiveData<Resources<List<TaskModel>>>  get() = _tasks

    fun getTasks(){
        _tasks = taskRepository.getTasksLiveData()
    }

    private val _taskStatus:MutableLiveData<Resources<String>> = MutableLiveData()
    val taskStatus:LiveData<Resources<String>> get() = _taskStatus

    private val _profileData:MutableLiveData<Resources<UserDataModel>> = MutableLiveData()
    val profileData:LiveData<Resources<UserDataModel>> get() = _profileData

    private val _profileStatus:MutableLiveData<Resources<String>> = MutableLiveData()
    val profileStatus:LiveData<Resources<String>> get() = _profileStatus

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
        _taskStatus.value = Resources.Loading()
        val response = repository.delete(taskId)
        _taskStatus.value = response
    }

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> get() = _rooms

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> get() = _chats

    fun loadRooms(userId: String) {
        viewModelScope.launch {
            _rooms.value = chatRepository.getRoomsForUser(userId)
        }
    }

    fun loadChats(roomId: String) {
        viewModelScope.launch {
            _chats.value = chatRepository.getChatsForRoom(roomId)
        }
    }

    fun sendMessage(roomId: String, chat: Chat) {
        viewModelScope.launch {
            chatRepository.sendMessage(roomId, chat)
            loadChats(roomId) // Reload chats to update UI
        }
    }

    fun profileUpdate(userId: String,userData:UserDataModel){
        _profileStatus.value = Resources.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val result = profileRepository.setupProfile(userId,userData)
            withContext(Dispatchers.Main){
                _profileStatus.value = result
            }
        }
    }

}