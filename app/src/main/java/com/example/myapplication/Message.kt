package com.example.myapplication

data class Message(
    var message: String?,
    var sendId: String?
){
    constructor():this("","")
}