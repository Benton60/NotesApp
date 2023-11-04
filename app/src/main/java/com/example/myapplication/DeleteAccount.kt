package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.InputStreamReader

class DeleteAccount : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)
        val btnYes = findViewById<Button>(R.id.btnYesIWantToDeleteMyAccount)
        val btnNo = findViewById<Button>(R.id.btnNoIDontWantToDeleteMyAccount)
        btnNo.setOnClickListener {
            finish()
        }
        btnYes.setOnClickListener {
            deleteUserFromServer()
        }
    }

    fun deleteUserFromServer() = CoroutineScope(Dispatchers.IO).launch{
        val usersRef = Firebase.firestore.collection("users")
        val user = usersRef
            .whereEqualTo("name", loadThisNote("userUsername.usr"))
            .get()
            .await()
        for (document in user){
            usersRef.document(document.id).delete()
        }
        for (file in filesDir.listFiles().filter { it.exists(); it.extension == "usr"}){
            file.delete()
        }
        finish()
    }
    private fun loadThisNote(noteName: String): String {
        return try {
            val fileInputStream = openFileInput(noteName)
            val inputReader = InputStreamReader(fileInputStream)
            val output = inputReader.readText()
            output
        }catch(e: IOException){
            ""
        }
    }
}