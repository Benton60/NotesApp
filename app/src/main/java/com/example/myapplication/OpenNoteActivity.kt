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
    private val  usersRef = Firebase.firestore.collection("users")
    private var userExists = "true"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        userExists = intent.getStringExtra("EXTRA_USER-EXISTS").toString()

        setText(loadThisNote(getNoteName()))

        btnSave.setOnClickListener {
            addDataToFireStore()
            if(saveThisNote(getNoteName())){

            }else{
                setText("Failed")
            }
        }

        btnDelete.setOnClickListener {
            deleteThisNote(getNoteName())
            finish()
        }
    }


    private fun addDataToFireStore() = CoroutineScope(Dispatchers.IO).launch {
        saveThisNote(getNoteName())
        val db = Firebase.firestore
        val notesToAddToDB = hashMapOf(
            " " to " "
        )
        filesDir.listFiles()?.filter {
            it.canRead()
            it.isFile
            it.canWrite()
            it.exists()
            it.extension == "note"
        }?.forEach {
            notesToAddToDB.put(it.name, loadThisNote(it.name))
        }
        notesToAddToDB.remove(" ")
        if(userExists.equals("true")){
            val user = usersRef
                .whereEqualTo("user", intent.getStringExtra("EXTRA_USERNAME").toString())
                .get()
                .await()
            for (document in user){
                usersRef.document("lastTime").set (
                    SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()),
                    SetOptions.merge()
                ).await()
                usersRef.document("notes").set(
                    notesToAddToDB,
                    SetOptions.merge()
                ).await()
            }
        }else {
            val user = hashMapOf(
                "user" to intent.getStringExtra("EXTRA_USERNAME").toString(),
                "password" to intent.getStringExtra("EXTRA_PASSWORD").toString(),
                "lastTime" to SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()),
                "notes" to notesToAddToDB
            )
            //val toast = Toast.makeText(this, "Connect To The Internet", Toast.LENGTH_SHORT)
            //toast.show()
            db.collection("users")
                .add(user)
                .addOnSuccessListener {
                    //val toast = Toast.makeText(this, "Saved", Toast.LENGTH_LONG)
                    //toast.show()
                }
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
}