package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)
        val btnBack = findViewById<Button>(R.id.btnSettingsBack)
        val btnLogin = findViewById<Button>(R.id.btnLoginSettings)
        val btnBugReport = findViewById<Button>(R.id.btnBugReport)

        checkUserLogin()

        btnBugReport.setOnClickListener {
            if(checkForInternet(this)){
                Intent(this, BugReportActivity::class.java).also{
                    startActivity(it)
                }
            }else{
                Toast.makeText(this, "Please connect to the internet to report a bug.", Toast.LENGTH_LONG).show()
            }

        }
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