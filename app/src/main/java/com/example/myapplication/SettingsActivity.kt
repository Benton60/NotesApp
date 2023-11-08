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
        val btnLogin = findViewById<Button>(R.id.btnLoginSettings)

        checkUserLogin()
        btnLogin.setOnClickListener {
            login()
        }
        btnBack.setOnClickListener {
            finish()
        }
        btnDeleteAccount.setOnClickListener {
            Intent(this, DeleteAccount::class.java).also{
                startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkUserLogin()
    }
    private fun login(){
        Intent(this, LoginActivity::class.java).also{
            startActivity(it)
        }
    }
    private fun checkUserLogin(){
        val btnLogin = findViewById<Button>(R.id.btnLoginSettings)
        btnLogin.text = "Login"
        filesDir.listFiles().filter {
            it.name == "userPassword.usr"
        }.forEach{
            btnLogin.text = "Signed In"
        }
    }
}