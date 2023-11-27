package com.example.crayonmarket.view.signUp

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
import com.example.crayonmarket.databinding.ActivitySignUpBinding
import com.example.crayonmarket.view.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, SignUpActivity::class.java)
        }
    }

    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        confirmPassword.addTextChangedListener {
            if (it != null) {
                viewModel.updateConfirmPassword(it.toString())
            }
        }
        binding.signUpButton.setOnClickListener {
            viewModel.signUp()
        }
    }

    private fun updateUi(uiState: SignUpUiState) = with(binding) {
        emailInputLayout.apply {
            isErrorEnabled = uiState.showEmailError
            error = if (uiState.showEmailError) {
                context.getString(R.string.email_is_not_valid)
            } else null
        }

        passwordInputLayout.apply {
            isErrorEnabled = uiState.showPasswordError
            error = if (uiState.showPasswordError) {
                context.getString(R.string.password_is_not_valid)
            } else null
        }

        confirmPasswordInputLayout.apply {
            isErrorEnabled = uiState.showConfirmPasswordError
            error = if (uiState.showConfirmPasswordError) {
                context.getString(R.string.confirmName_is_not_valid)
            } else null
        }

        if (uiState.successToSignUp) {
            navigateUserInfoView()
        }
        if (uiState.errorMessage != null) {
            showSnackBar(uiState.errorMessage)
            viewModel.userMessageShown()
        }

        signUpButton.apply {
            isEnabled = uiState.isInputValid && !uiState.isLoading
            setText(if (uiState.isLoading) R.string.loading else R.string.registerText)
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(this, binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateUserInfoView() {
        val intent = UserInfoActivity.getIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}