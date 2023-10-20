package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val userName = "Admin"
    private val password = "Admin"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtFileName = findViewById<EditText>(R.id.edtFileName)
        val btnBigBoi = findViewById<Button>(R.id.btnBigBoi)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        showFiles()
        checkIfLoggedIn()
        btnLogin.setOnClickListener {
            login()
        }
        btnBigBoi.setOnClickListener {
            if(edtFileName.text.toString() != "") {
                Intent(this, OpenNoteActivity::class.java).also {
                    it.putExtra("EXTRA_FILENAME", edtFileName.text.toString() + ".note")
                    it.putExtra("EXTRA_USERNAME", userName)
                    it.putExtra("EXTRA_PASSWORD", password)
                    startActivity(it)
                }
            }else{
                Toast.makeText(this, "Please Enter A File Name", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        checkIfLoggedIn()
        showFiles()
    }
    private fun login(){
        Intent(this, LoginActivity::class.java).also{
            startActivity(it)
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
    private fun checkIfLoggedIn(){
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.text = "Login"
        filesDir.listFiles().filter{
            it.isFile
            it.canRead()
            it.exists()
            it.extension == "usr"
        }.forEach{
            if(it.nameWithoutExtension == "userPassword") {
                btnLogin.text = "Signed In"
            }
        }
    }
    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}