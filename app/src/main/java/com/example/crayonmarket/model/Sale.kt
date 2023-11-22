package com.example.crayonmarket.model

data class Sale(
    val uuid: String,
    val title: String,
    val writerUuid: String,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val content: String,
    val imageUrl: String,
    val isMine: Boolean,
    val time: String,
    val cost: Long,
    val possibleSale: Boolean
)