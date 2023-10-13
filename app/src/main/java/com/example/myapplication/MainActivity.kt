package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtFileName = findViewById<EditText>(R.id.edtFileName)
        val btnBigBoi = findViewById<Button>(R.id.btnBigBoi)

        showFiles()
        btnBigBoi.setOnClickListener {
            Intent(this, OpenNoteActivity::class.java).also{
                it.putExtra("EXTRA_FILENAME", edtFileName.text.toString())
                startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showFiles()
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
        }?.forEach {
            text += "\n" + it.name
        }
        tvListFiles.text = text
    }
}