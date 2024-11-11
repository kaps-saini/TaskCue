package com.task.taskCue.presentation.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.task.taskCue.R
import com.task.taskCue.databinding.FragmentAuthenticationBinding
import com.task.taskCue.domain.models.ResendTokenData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import com.task.taskCue.data.local.SharedPrefManager
import com.task.taskCue.helper.Helper
import com.task.taskCue.presentation.vm.TaskViewModel
import com.task.taskCue.utils.AuthResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class Authentication : Fragment() {

    private var _binding: FragmentAuthenticationBinding? = null
    private val binding get() = _binding!!

    private var ccp: CountryCodePicker? = null
    private val auth = FirebaseAuth.getInstance()
    private val viewModel by viewModels<TaskViewModel>()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInOptions: GoogleSignInOptions
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_authentication, container, false)

        // Initialize GoogleSignInOptions
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // Make sure the web client ID is correct
            .requestEmail()
            .build()

        // Initialize GoogleSignInClient
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userEvents()
        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authStatus.collect { result ->
                    when (result) {
                        is AuthResult.Error -> Helper.makeSnackBar(requireView(),result.message)
                        AuthResult.Loading ->{}
                        is AuthResult.Success -> {
                            sharedPrefManager.setLoginStatus(true)
                            findNavController().navigate(R.id.action_authentication_to_homeFragment)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun userEvents(){
        //Login through mobile no
        binding.btnSignIn.setOnClickListener {
            val num = validateMobileNumber()
            if (num.isNotEmpty()){
                mobileAuth(num)
            }
        }

        //Login through email
        binding.btnGoogleSignIn.setOnClickListener {
            signIn(googleSignInClient)
        }
    }

    private fun validateMobileNumber():String {
        val mobNum = binding.etMobileNumber.text.toString()
        ccp = binding.ccp

        if (mobNum.isNotEmpty()){
            if (mobNum.length == 10){
                val num = ccp!!.selectedCountryCodeWithPlus + mobNum
                return num.trim()
            }else{
                binding.tilNumber.helperText = "Enter a valid number"
            }
        }else{
            binding.tilNumber.helperText = "Enter a mobile number"
        }
        return ""
    }

    private fun mobileAuth(phoneNumber:String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.i("TAG", e.message.toString())
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            val action = AuthenticationDirections.actionAuthenticationToOtpConfirmation(
                phoneNumber = binding.etMobileNumber.text.toString(),
                verificationId = verificationId,
                ResendTokenData(token)
            )
            findNavController().navigate(action)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    findNavController().navigate(R.id.action_authentication_to_homeFragment)
                    sharedPrefManager.setLoginStatus(true)
                } else {
                    // Sign in failed, display a message and update the UI

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (sharedPrefManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_authentication_to_homeFragment)
        }
    }

    private fun signIn(googleSignInClient: GoogleSignInClient) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let {
                    viewModel.signInWithGoogle(it)
                }
            } catch (e: ApiException) {
                Log.e("Auth",e.message.toString())
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}