package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date

class OpenNoteActivity : AppCompatActivity() {
    private val usersRef = Firebase.firestore.collection("users")
    private var currentUser = User(" ", " ", hashMapOf("" to ""), " ")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        updateUserInfo()

        setText(loadThisNote(getNoteName()))

        btnSave.setOnClickListener {

            if(saveThisNote(getNoteName())){
                updateUserInfo()
                updateDataToFireStore()
            }else{
                setText("Failed")
            }
        }

        btnDelete.setOnClickListener {
            deleteThisNote(getNoteName())
            updateUserInfo()
            updateDataToFireStore()
            finish()
        }
    }
    private fun updateDataToFireStore() = CoroutineScope(Dispatchers.IO).launch {
        val user = usersRef
            .whereEqualTo("name", currentUser.name)
            .get()
            .await()
        if(!user.isEmpty){
            for (document in user){
                usersRef.document(document.id).set (
                    currentUser.getHashMapOf(),
                    SetOptions.merge()
                ).await()
            }
        }else {
            val db = Firebase.firestore
            db.collection("users")
                .add(currentUser.getHashMapOf())
        }
        finish()
    }
    private fun getNoteName(): String {
         return intent.getStringExtra("EXTRA_FILENAME").toString()
    }
    private fun getText(): String {
        val edtNote = findViewById<EditText>(R.id.edtNote)
        return edtNote.text.toString()
    }
    private fun setText(note: String?){
        val edtNote = findViewById<EditText>(R.id.edtNote)
        edtNote.setText(note, TextView.BufferType.EDITABLE)
    }
    private fun saveThisNote(noteName: String): Boolean{
        return try{
            val fileOutputStream = openFileOutput(noteName, MODE_PRIVATE)
            val outputWriter = OutputStreamWriter(fileOutputStream)
            outputWriter.write(getText())
            outputWriter.close()
            true
        }catch(e: IOException){
            println(e)
            false
        }
    }
    private fun deleteThisNote(noteName: String): Boolean{
        return try{
            deleteFile(noteName)
            true
        }catch(e: Exception){
            e.printStackTrace()
            false
        }
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
    private fun updateUserInfo(){
        currentUser.name = intent.getStringExtra("EXTRA_USERNAME").toString()
        currentUser.password = intent.getStringExtra("EXTRA_PASSWORD").toString()
        currentUser.lastTime  = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())

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
        currentUser.notes = notesToAddToUser
    }
}