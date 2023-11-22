package com.example.crayonmarket.firebase.dto

import java.util.Date

data class SaleDto(
    val uuid: String = "",
    val title: String = "",
    val cost: Long = 0,
    val content: String = "",
    val imageUrl: String = "",
    val writerUuid: String = "",
    val time: Date = Date(),
    val possibleSale: Boolean = true
)