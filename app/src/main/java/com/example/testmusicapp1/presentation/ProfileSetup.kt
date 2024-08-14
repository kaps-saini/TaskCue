package com.example.testmusicapp1.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.testmusicapp1.R
import com.example.testmusicapp1.databinding.FragmentProfileSetupBinding
import com.example.testmusicapp1.helper.Helper
import com.example.testmusicapp1.models.UserDataModel
import com.example.testmusicapp1.presentation.vm.TaskViewModel
import com.google.android.gms.cast.framework.media.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class ProfileSetup : Fragment() {

    private var _binding: FragmentProfileSetupBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ProfileSetupArgs>()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val viewModel by viewModels<TaskViewModel>()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private var imageUrl: String? = null
    private lateinit var selectedImage:Uri
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri ->
            if (imageUri != null) {
                binding.ivUserImage.setImageURI(imageUri)
                selectedImage = imageUri
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_setup, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchProfileData()

        val genderOptions = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.acUserGender.setAdapter(adapter)

        binding.btnSubmit.setOnClickListener {
            profileUpdate()
        }

        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnVisibility()
            }
        })

        binding.ivUserImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideProgressBar() {
        binding.pbProfile.visibility = View.INVISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
    }

    private fun showProgressBar() {
        binding.pbProfile.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.INVISIBLE
    }

    private fun profileUpdate() {
        val userName = binding.etUserName.text.toString()
        val userGender = binding.acUserGender.text.toString()
        val userMobile = args.MobileNo
        val userId = auth.currentUser?.uid.toString()

        var isValid = true

        if (userName.isEmpty()) {
            isValid = false
            Helper.makeSnackBar(requireView(), "Please enter name")
        }
        if (userGender.isEmpty()) {
            isValid = false
            Helper.makeSnackBar(requireView(), "Please enter gender")
        }

        if (isValid) {
            showProgressBar()

            // Check if an image is selected
            if (::selectedImage.isInitialized) {
                // If image is selected, upload it first
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val downloadUrl = uploadImageToFirebaseStorage(selectedImage)
                        if (downloadUrl != null) {
                            imageUrl = downloadUrl
                            Log.d("FirebaseStorage", "Image URL: $downloadUrl")
                        }
                        // After image upload (or if skipped), proceed with profile update
                        saveUserProfile(userId, userName, userMobile, imageUrl, userGender)
                    } catch (e: Exception) {
                        Log.e("FirebaseStorage", "Error uploading image: ${e.message}")
                        hideProgressBar()
                    }
                }
            } else {
                // If no image is selected, proceed directly with profile update
                saveUserProfile(userId, userName, userMobile, imageUrl, userGender)
            }
        }
    }

    private suspend fun uploadImageToFirebaseStorage(imageUri: Uri): String? {
        return try {
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference
            val fileReference: StorageReference = storageReference.child("images/${UUID.randomUUID()}.jpg")
            val downloadUrl = fileReference.putFile(imageUri).await().storage.downloadUrl.await().toString()
            downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error uploading image: ${e.message}")
            null
        }
    }

    private fun saveUserProfile(userId: String, userName: String, userMobile: String, imageUrl: String?, userGender: String) {
        val userData = UserDataModel(userId, userName, userMobile, imageUrl ?: "", userGender)
        usersCollection.document(userId).set(userData).addOnCompleteListener {
            if (it.isSuccessful) {
                hideProgressBar()
                Helper.makeSnackBar(requireView(), "Profile set up successfully")
                findNavController().navigate(R.id.action_profileSetup_to_homeFragment)
            } else {
                hideProgressBar()
                Helper.makeSnackBar(requireView(), "Failed to update profile")
            }
        }.addOnFailureListener {
            hideProgressBar()
            Helper.makeSnackBar(requireView(), "Error: ${it.message}")
        }
    }


    private fun btnVisibility() {
        val userName = binding.etUserName.text.toString()
        if (userName.isEmpty()) {
            binding.btnSubmit.alpha = 0.4f
            binding.btnSubmit.isActivated = false
            binding.btnSubmit.isClickable = false
        } else {
            binding.btnSubmit.alpha = 1f
            binding.btnSubmit.isActivated = true
            binding.btnSubmit.isClickable = true
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun fetchProfileData() {
        val defaultImage: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.user_default_image)
        args.UserData?.let {
            binding.etUserName.setText(it.userName)
            binding.acUserGender.setText(it.gender)
            binding.tvUserMobile.text = it.mobileNo
            Glide.with(this)
                .load(it.userImage)
                .apply(RequestOptions().error(defaultImage))
                .into(binding.ivUserImage)
            binding.btnSubmit.text = "Update"
        }
    }

//    private suspend fun uploadImageToFirebaseStorage(imageUri: Uri): String? = suspendCoroutine { continuation ->
//        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
//        val fileReference: StorageReference = storageReference.child("images/${UUID.randomUUID()}.jpg")
//
//        fileReference.putFile(imageUri)
//            .addOnSuccessListener { taskSnapshot ->
//                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
//                    val downloadUrl = uri.toString()
//                    continuation.resume(downloadUrl)
//                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
//                }.addOnFailureListener { exception ->
//                    continuation.resumeWithException(exception)
//                    Toast.makeText(requireContext(), "Failed to retrieve download URL", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { exception ->
//                continuation.resumeWithException(exception)
//                Toast.makeText(requireContext(), "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

}
