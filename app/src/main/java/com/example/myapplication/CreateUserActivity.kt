package com.example.myapplication

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
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date

class CreateUserActivity : AppCompatActivity() {
    var userToCreate = User("", "", hashMapOf<String, Any>("" to ""), "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        val btnCheckToCreate = findViewById<Button>(R.id.btnCheckToCreate)
        val edtUsernameToCreate = findViewById<EditText>(R.id.edtUsernameToCreate)
        val edtPasswordToCreate = findViewById<EditText>(R.id.edtPasswordToCreate)
        val btnBackToCreate = findViewById<Button>(R.id.btnBackToCreate)
        val edtPasswordToCreateSecond = findViewById<EditText>(R.id.edtPasswordToCreateSecond)

        btnBackToCreate.setOnClickListener {
            finish()
        }
        btnCheckToCreate.setOnClickListener {
            createNewUser(edtUsernameToCreate.text.toString(), edtPasswordToCreate.text.toString(), edtPasswordToCreateSecond.text.toString())
        }

    }

    private fun createNewUser(username: String, password: String, passwordSecond: String) = CoroutineScope(Dispatchers.IO).launch {
        val usersRef = Firebase.firestore.collection("users")
        val user = usersRef
            .whereEqualTo("name", username)
            .get()
            .await()
        if (user.isEmpty && ((username != "" && password != "") && (password.equals(passwordSecond)))){
            updateUserInfo(username, password)
            val db = Firebase.firestore
            db.collection("users")
                .add(userToCreate.getHashMapOf())
            finish()
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CreateUserActivity,
                    "Username Already Exists or Is Invalid",
                    Toast.LENGTH_LONG
                ).show()
            }
            val edtUsernameToCreate = findViewById<EditText>(R.id.edtUsernameToCreate)
            val edtPasswordToCreate = findViewById<EditText>(R.id.edtPasswordToCreate)
            val edtPasswordToCreateSecond = findViewById<EditText>(R.id.edtPasswordToCreateSecond)
            edtUsernameToCreate.setText("")
            edtPasswordToCreate.setText("")
            edtPasswordToCreateSecond.setText("")
        }

    }
    private fun updateUserInfo(username: String, password: String){
        userToCreate.name = username
        userToCreate.password = password
        userToCreate.lastTime  = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())

        val notesToAddToUser = hashMapOf<String, Any>(
            " " to " "
        )
        filesDir.listFiles()?.filter {
            it.canRead()
            it.isFile
            it.canWrite()
            it.exists()
            it.extension == "note"
        }?.forEach {
            notesToAddToUser[it.name] = loadThisNote(it.name)
        }
        notesToAddToUser.remove(" ")
        userToCreate.notes = notesToAddToUser
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