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

        setText(loadThisNote("file.txt"))
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            if(saveThisNote("file")){
                finish()
            }
            finish()
        }
    }
    fun getText(): String {
        val edtNote = findViewById<EditText>(R.id.edtNote)
        return edtNote.text.toString()
    }

    fun setText(note: String?){
        val edtNote = findViewById<EditText>(R.id.edtNote)
        edtNote.setText(note, TextView.BufferType.EDITABLE)
    }

    private fun saveThisNote(noteName: String): Boolean{
        return try{
            openFileOutput("$noteName.txt", MODE_PRIVATE).use {
                val outputWriter = OutputStreamWriter(it)
                outputWriter.write(getText())
            }
            true
        }catch(e: IOException){
            e.printStackTrace()
            false
        }
    }
    private fun loadThisNote(noteName: String): String? {
        try {
            openFileInput(noteName).use {
                val inputReader = InputStreamReader(it)
                val noteRecievedFromFile = inputReader.read().toString()
                return noteRecievedFromFile
            }
        }catch(e: IOException){
            return ""
        }
    }
}