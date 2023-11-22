package com.example.crayonmarket.firebase.dto

data class UserDto(
    val uuid: String = "",
    val name: String = "",
    val email: String? = null,
    val password: String? = null,
    val profileImageUrl: String? = null
)