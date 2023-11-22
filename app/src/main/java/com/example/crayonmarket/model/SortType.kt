package com.example.crayonmarket.model

enum class SortType(val sortNumber : Int, val sortType : String) {
    LATEST_ORDER(0, "최신순"),
    HIGH_PRICE_ORDER(1,"높은 가격순"),
    LOW_PRICE_ORDER(2,"낮은 가격순")
}