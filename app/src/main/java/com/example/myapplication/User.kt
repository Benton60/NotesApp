package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Date


class User (
    var name: String = "",
    var password: String = "",
    var notes: HashMap<String, Any>,
    var lastTime: String = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
){
    fun getHashMapOf(): HashMap<String, Any> {
        return hashMapOf(
            "name" to name,
            "password" to password,
            "notes" to notes,
            "lastTime" to SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
        )
    }
}

