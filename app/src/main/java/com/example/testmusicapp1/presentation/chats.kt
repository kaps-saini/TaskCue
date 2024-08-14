package com.example.testmusicapp1.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.testmusicapp1.R
import com.example.testmusicapp1.databinding.FragmentChatsBinding
import com.example.testmusicapp1.models.UserDataModel
import com.example.testmusicapp1.presentation.vm.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class chats : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val viewmodel by viewModels<TaskViewModel>()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private lateinit var userList:MutableList<UserDataModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chats, container, false)


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

}