package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)
        val btnBack = findViewById<Button>(R.id.btnSettingsBack)


        btnBack.setOnClickListener {
            finish()
        }
        btnDeleteAccount.setOnClickListener {
            Intent(this, DeleteAccount::class.java).also{
                startActivity(it)
            }
        }
    }
}