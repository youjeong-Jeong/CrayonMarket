package com.example.crayonmarket.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.crayonmarket.R
import com.example.crayonmarket.databinding.ActivityLoginBinding
import com.example.crayonmarket.view.main.MainActivity
import com.example.crayonmarket.view.signUp.SignUpActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.signedIn) {
            navigateToMainView()
        }

        initEventListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUi)
            }
        }
    }

    private fun initEventListeners() = with(binding) {
        email.addTextChangedListener {
            if (it != null) {
                viewModel.updateEmail(it.toString())
            }
        }
        password.addTextChangedListener {
            if (it != null) {
                viewModel.updatePassword(it.toString())
            }
        }
        signInButton.setOnClickListener {
            viewModel.signIn()
        }
        signUpText.setOnClickListener {
            navigateToSignUpView()
        }
    }

    private fun updateUi(uiState: LoginUiState) {
        binding.emailInputLayout.apply {
            isErrorEnabled = uiState.showEmailError
            error = if (uiState.showEmailError) {
                context.getString(R.string.email_is_not_valid)
            } else null
        }
        binding.passwordInputLayout.apply {
            isErrorEnabled = uiState.showPasswordError
            error = if (uiState.showPasswordError) {
                context.getString(R.string.password_is_not_valid)
            } else null
        }

        if (uiState.successToLogin) {
            onSuccessToLogin()
        }
        if (uiState.errorMessage != null) {
            showSnackBar(uiState.errorMessage)
            viewModel.userMessageShown()
        }
        binding.signInButton.apply {
            isEnabled = uiState.isInputValid && !uiState.isLoading
            setText(if (uiState.isLoading) R.string.loading else R.string.login)
        }
    }

    private fun onSuccessToLogin() {
        viewModel.checkUserInfoExists { exists ->
            if (exists) {
                val sharedPreferences = getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )
                sharedPreferences.edit().putBoolean(getString(R.string.prefs_has_user_info), true)
                    .apply()

                navigateToMainView()
            }
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToSignUpView() {
        val intent = SignUpActivity.getIntent(this)
        startActivity(intent)
    }

    private fun navigateToMainView() {
        val intent = MainActivity.getIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        }
        startActivity(intent)
        finish()
    }
}