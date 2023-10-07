package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnBigBoi = findViewById<Button>(R.id.btnBigBoi)
        btnBigBoi.setOnClickListener {
            switchButton()
        }
    }
    fun switchButton(){
        val textView = findViewById<TextView>(R.id.textView)
        if(textView.text.toString() == "bois"){
            textView.text = "YUUUUUHHHH"
        }else{
            textView.text = "bois"
        }
    }
}