package com.example.myapplication



class User (
    var name: String = "",
    var password: String = "",
    var notes: HashMap<String, Any>,
    var lastTime: String
){
    fun getHashMapOf(): HashMap<String, Any> {
        return hashMapOf(
            "name" to name,
            "password" to password,
            "notes" to notes,
            "lastTime" to lastTime
        )
    }
}

