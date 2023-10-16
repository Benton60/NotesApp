package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private val  usersRef = Firebase.firestore.collection("users")
    private val userName = "Admin"
    private val password = "Admin"
    private var userExists = "false"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtFileName = findViewById<EditText>(R.id.edtFileName)
        val btnBigBoi = findViewById<Button>(R.id.btnBigBoi)
        checkIfUserExists()
        showFiles()
        btnBigBoi.setOnClickListener {
            Intent(this, OpenNoteActivity::class.java).also {
                it.putExtra("EXTRA_FILENAME", edtFileName.text.toString() + ".note")
                it.putExtra("EXTRA_USERNAME", userName)
                it.putExtra("EXTRA_PASSWORD", password)
                it.putExtra("EXTRA_USER-EXISTS", userExists)
                startActivity(it)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        checkIfUserExists()
        showFiles()
    }

    private fun checkIfUserExists() = CoroutineScope (Dispatchers.IO).launch{
        try {
            val userQuery = usersRef
                .whereEqualTo("user", userName)
                .get()
                .await()
            if (userQuery.documents.isNotEmpty()) {
                userExists = "true"
            }
        }catch(e: Exception){

        }
    }

    private fun showFiles() {
        val edtFileName = findViewById<EditText>(R.id.edtFileName)
        val tvListFiles = findViewById<TextView>(R.id.tvListFiles)

        edtFileName.setText("")
        tvListFiles.text = ""
        var text = ""
        filesDir.listFiles()?.filter {
            it.canRead()
            it.isFile
            it.canWrite()
            it.exists()
            it.extension == "note"
        }?.forEach {
            text += "\n" + it.nameWithoutExtension
        }
        tvListFiles.text = text
    }
}