package com.task.taskCue.presentation.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.task.taskCue.R
import com.task.taskCue.databinding.FragmentProfileBinding
import com.task.taskCue.helper.Helper
import com.task.taskCue.helper.Helper.showDeleteAccountDialog
import com.task.taskCue.helper.Helper.showLogOutDialog
import com.task.taskCue.domain.models.UserDataModel
import com.task.taskCue.presentation.vm.TaskViewModel
import com.task.taskCue.utils.TaskResult
import com.task.taskCue.utils.Utils.APP_VERSION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val viewModel by viewModels<TaskViewModel>()
    private lateinit var userDataModel: UserDataModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false)

        viewModel.getProfileData()
        viewModel.profileData.observe(viewLifecycleOwner){ response->
            when(response){
                is TaskResult.Error -> Helper.makeSnackBar(requireView(), response.toString())
                is TaskResult.Loading -> Helper.makeSnackBar(requireView(),"Loading")
                is TaskResult.Success -> fetchProfileData(response)
            }
        }

        binding.tvSignOut.setOnClickListener {
            showLogOutDialog(requireContext()){
                auth.signOut()
                if (auth.currentUser == null){
                    findNavController().navigate(R.id.action_profile_to_authentication)
                }
            }
        }

        binding.btnDeleteAccount.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                showDeleteAccountDialog(requireContext()) {
                    auth.currentUser?.delete()?.addOnSuccessListener {
                        deleteFirebaseData(userId) { deletionSuccessful ->
                            if (deletionSuccessful) {
                                Helper.makeSnackBar(requireView(),"Account deleted successfully")
                                findNavController().navigate(R.id.action_profile_to_authentication)
                            }
                        }
                    }?.addOnFailureListener { exception ->
                        // Handle the failure, maybe show an error message
                        Helper.makeSnackBar(requireView(),"Account deletion failed: ${exception.message}")
                    }
                }
            } else {
                // Handle the case where the user is null, though this should be rare
                Helper.makeSnackBar(requireView(),"User is null")
            }
        }


        binding.btnUpdateProfile.setOnClickListener {
            val action = ProfileDirections.actionProfileToProfileSetup(userDataModel.mobileNo,userDataModel)
            findNavController().navigate(action)
        }

        binding.tvAppVersion.text = APP_VERSION

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun fetchProfileData(response: TaskResult.Success<UserDataModel>) {
        val name = response.data.userName
        val mobile = response.data.mobileNo
        val gender = response.data.gender
        val image = response.data.userImage
        val id = auth.currentUser?.uid.toString()

        // Default image resource
        val defaultImage: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.user_default_image)

        userDataModel = UserDataModel(id, name, mobile, image, gender)

        // Set profile text fields
        binding.profileName.text = name
        binding.profileMobile.text = mobile
        binding.profileGender.text = gender

        // Load image with Glide and handle failures
        Glide.with(requireView())
            .load(image)
            .apply(RequestOptions().error(defaultImage)) // Use defaultImage if loading fails
            .into(binding.profileImage)
    }

    private fun deleteFirebaseData(userId:String,callback:(Boolean)->Unit){
        db.collection("users").document(userId).delete().addOnCompleteListener {
            if (it.isSuccessful){
                callback(true)
            }
        }
            .addOnFailureListener {
                callback(false)
                Helper.makeSnackBar(requireView(),"Something went wrong")
            }
    }
}