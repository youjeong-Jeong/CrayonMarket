package com.example.crayonmarket.view.common

fun Long.toCostString(): String {
    val str = "%,d".format(this)
    return "${str}원"
}