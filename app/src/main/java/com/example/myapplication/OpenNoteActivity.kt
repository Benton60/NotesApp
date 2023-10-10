package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class OpenNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        setText(loadThisNote("file.txt"))

        btnSave.setOnClickListener {
            if(saveThisNote("file.txt")){
                finish()
            }else{
                setText("Failed")
            }
        }

        btnDelete.setOnClickListener {
            deleteThisNote("file.txt")
            finish()
        }
    }
    fun getNoteName(){

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