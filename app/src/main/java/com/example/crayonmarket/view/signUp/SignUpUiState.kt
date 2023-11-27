package com.example.crayonmarket.view.signUp

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val successToSignUp: Boolean = false,
    val errorMessage: String? = null
) {
    val isInputValid: Boolean
        get() = isEmailValid && isPasswordValid && isConfirmPasswordValid

    private val isEmailValid: Boolean
        get() {
            return if (email.isEmpty()) {
                false
            } else {
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        }

    private val isPasswordValid: Boolean
        get() = password.length >= 8

    private val isConfirmPasswordValid: Boolean
        get() = confirmPassword == password

    val showEmailError: Boolean
        get() = email.isNotEmpty() && !isEmailValid

    val showPasswordError: Boolean
        get() = password.isNotEmpty() && !isPasswordValid

    val showConfirmPasswordError: Boolean
        get() = confirmPassword.isNotEmpty() && !isConfirmPasswordValid
}