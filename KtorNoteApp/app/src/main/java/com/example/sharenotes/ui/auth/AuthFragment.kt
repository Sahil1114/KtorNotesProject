package com.example.sharenotes.ui.auth

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.sharenotes.R
import com.example.sharenotes.data.remote.BasicAuthInterceptor
import com.example.sharenotes.databinding.FragmentAuthBinding
import com.example.sharenotes.ui.BaseFragment
import com.example.sharenotes.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.example.sharenotes.utils.Constants.KEY_PASSWORD
import com.example.sharenotes.utils.Constants.NO_EMAIL
import com.example.sharenotes.utils.Constants.NO_PASSWORD
import com.example.sharenotes.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment:BaseFragment(R.layout.fragment_auth) {


    private val viewModel:AuthViewModel by viewModels()

    private lateinit var binding: FragmentAuthBinding

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    @Inject
    lateinit var sharedPref:SharedPreferences

    private var currEmail:String?=null
    private var currPassword:String?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        subscribeToObserver()

        if(isLoggedIn()){
            authenticateApi(currEmail?:"",currPassword?:"")
            redirectLogin()
        }

        binding.apply {
             btnRegister.setOnClickListener {
                val email=binding.etRegisterEmail.text.toString()
                val password=binding.etRegisterPassword.text.toString()
                val comfirmedPassword=binding.etRegisterPasswordConfirm.text.toString()

                viewModel.register(email,password,comfirmedPassword)

            }
             btnLogin.setOnClickListener {
                val email=etLoginEmail.text.toString()
                val password=etLoginPassword.text.toString()
                 currEmail=email
                 currPassword=password
                 viewModel.login(email,password)
            }
        }
    }

    private fun authenticateApi(email: String, password: String) {
        basicAuthInterceptor.email=email
        basicAuthInterceptor.password=password
    }

    private fun redirectLogin(){
        val navOptions=NavOptions.Builder()
            .setPopUpTo(R.id.authFragment,true)
            .build()
        findNavController().navigate(
            AuthFragmentDirections.actionAuthFragmentToNotesFragment(),
            navOptions
        )
    }

    private fun isLoggedIn():Boolean{
        currEmail=sharedPref.getString(KEY_LOGGED_IN_EMAIL, NO_EMAIL)?: NO_EMAIL
        currPassword=sharedPref.getString(KEY_PASSWORD, NO_PASSWORD)?: NO_PASSWORD
        return currEmail!= NO_EMAIL&&currPassword!=null
    }

    private fun subscribeToObserver(){
        viewModel.loginStatus.observe(viewLifecycleOwner){result->
            result?.let {
                when(result.status){
                    Status.SUCCESS->{
                        binding.loginProgressBar.visibility=View.GONE
                        showSnackbar("Successfully logged in")
                        sharedPref.edit().putString(KEY_LOGGED_IN_EMAIL,currEmail).apply()
                        sharedPref.edit().putString(KEY_PASSWORD,currPassword).apply()
                        authenticateApi(currEmail?:"",currPassword?:"")
                        redirectLogin()
                    }
                    Status.ERROR->{
                        binding.loginProgressBar.visibility=View.GONE
                        showSnackbar(result.message?:"An unknown error occured")
                    }
                    Status.LOADING->{
                        binding.loginProgressBar.visibility=View.VISIBLE
                    }
                }
            }
        }
        viewModel.registerStatus.observe(viewLifecycleOwner){result->
            result?.let {
                when(result.status){
                    Status.SUCCESS->{
                        binding.registerProgressBar.visibility=View.GONE
                        showSnackbar(result.message?:"Successfully registered account")
                    }
                    Status.ERROR->{
                        binding.registerProgressBar.visibility=View.GONE
                        showSnackbar(result.data?:"An error occurred")
                    }
                    Status.LOADING->{
                        binding.registerProgressBar.visibility=View.VISIBLE
                    }

                }
            }

        }
    }

}