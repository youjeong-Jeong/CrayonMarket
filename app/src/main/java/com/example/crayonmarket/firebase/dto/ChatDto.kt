package com.example.crayonmarket.firebase.dto

import java.util.Date

data class ChatDto(
    val uuid: String = "",
    val userUuid: String = "",
    val message: String = "",
    val date: Date = Date(),
)