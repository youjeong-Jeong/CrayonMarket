package com.example.crayonmarket.firebase.dto

import java.util.Date

data class ChatRoomDto(
    val uuid: String = "",
    val postUuid: String = "",
    val conversationWriterUserUid: String = "",
    val conversationAppliedUserUid: String = "",
    val time: Date = Date()
)