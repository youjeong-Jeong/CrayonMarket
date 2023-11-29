package com.example.crayonmarket.model

data class UserDetail(
    val uuid: String,
    val name: String,
    val year : Int,
    val month : Int,
    val day : Int,
    val email: String?,
    val profileImageUrl: String?
) : java.io.Serializable