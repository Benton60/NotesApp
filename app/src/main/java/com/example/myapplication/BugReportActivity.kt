package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader

class BugReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bug_report)
        val btnBackBug = findViewById<Button>(R.id.btnBackBug)
        val btnSaveBug = findViewById<Button>(R.id.btnSaveBug)
        val edtBugDescription = findViewById<EditText>(R.id.edtBugDescription)

        btnSaveBug.setOnClickListener {
            if(edtBugDescription.text.toString() != "") {
                saveBug(edtBugDescription.text.toString())
            }else{
                Toast.makeText(this, "Please enter a valid bug", Toast.LENGTH_SHORT).show()
            }
        }
        btnBackBug.setOnClickListener {
            finish()
        }
    }
    private fun saveBug(description: String) = CoroutineScope(Dispatchers.IO).launch{
        withContext(Dispatchers.Main) {
            if (!checkForInternet(this@BugReportActivity)) {
                Toast.makeText(this@BugReportActivity, "Please connect to the internet to report a bug.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
        val bugRef = Firebase.firestore.collection("Bugs")
        val bug = hashMapOf<String, Any>(
            "bug" to description,
            "Username" to loadThisNote("userUsername.usr")
        )
        bugRef.add(bug).await()
        withContext(Dispatchers.Main){
            Toast.makeText(this@BugReportActivity, "Thank you for your Support!", Toast.LENGTH_LONG).show()
        }
        finish()
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