package com.example.uchat.models

data class User(
    val name:String,
    val imageUrl:String,
    val thumbImage:String,
    val deviceToken:String,
    val status:String,
    val online:Boolean,
    val uid:String
) {

    constructor():this("","","","","hey there, im using connectwing",false,"")

    constructor(name: String, imageUrl: String, thumbImage: String,uid: String):
            this(name,imageUrl,thumbImage, "",uid=uid,status = "Hey im using connectwing",online = false)

}