package com.bienbetter.application.model

data class User(
    var name: String,
    var email: String,
    var uId: String
){
    constructor(): this("", "", "")
}