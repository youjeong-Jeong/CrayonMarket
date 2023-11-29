package com.example.crayonmarket.firebase.dto

data class UserDto(
    val uuid: String = "",
    val name: String = "",
    val year : Int = 0,
    val month : Int = 0,
    val day : Int = 0,
    val email: String? = null,
    val password: String? = null,
    val profileImageUrl: String? = null
)