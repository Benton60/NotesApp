package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStreamWriter

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val btnCreateUser = findViewById<Button>(R.id.btnCreateUser)

        btnBack.setOnClickListener {
            deleteUserInfo()
            finish()
        }

        btnCheck.setOnClickListener {
            checkUserLoginInfo()
        }

        btnCreateUser.setOnClickListener {
            Intent(this, CreateUserActivity::class.java).also{
                startActivity(it)
            }
        }
    }
    private fun checkUserLoginInfo() = CoroutineScope(Dispatchers.IO).launch{
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val usersRef = Firebase.firestore.collection("users")

        val user = usersRef
            .whereEqualTo("name", edtUsername.text.toString())
            .whereEqualTo("password", edtPassword.text.toString())
            .get()
            .await()
        if(user.isEmpty){
            deleteUserInfo()
            edtPassword.setText("")

            withContext(Dispatchers.Main){
                Toast.makeText(this@LoginActivity, "Invalid Password", Toast.LENGTH_LONG).show()
            }
        }else{
            saveLogin()
            finish()
        }
    }
    private fun saveLogin(){
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        try{
            val fileOutputStream = openFileOutput("userPassword.usr", MODE_PRIVATE)
            val outputWriter = OutputStreamWriter(fileOutputStream)
            outputWriter.write(edtPassword.text.toString())
            outputWriter.close()

            val fileOutputStream2 = openFileOutput("userUsername.usr", MODE_PRIVATE)
            val outputWriter2 = OutputStreamWriter(fileOutputStream2)
            outputWriter2.write(edtUsername.text.toString())
            outputWriter2.close()
        }catch(e: IOException){
            println(e)
        }
    }
    private fun deleteUserInfo(){
        try {
            deleteFile("userPassword.usr")
            deleteFile("userUsername.usr")
            deleteFile("userLastUsed.sys")
        }catch(e: Exception){
            println(e)
        }
    }
}