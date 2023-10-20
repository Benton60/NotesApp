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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    var isNotUpToDate = false
    private val usersRef = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtFileName = findViewById<EditText>(R.id.edtFileName)
        val btnBigBoi = findViewById<Button>(R.id.btnBigBoi)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        showFiles()
        checkIfLoggedIn()
        checkIfUpToDate()
        btnLogin.setOnClickListener {
            login()
        }
        btnBigBoi.setOnClickListener {
            if(edtFileName.text.toString() != "") {
                Intent(this, OpenNoteActivity::class.java).also {
                    it.putExtra("EXTRA_FILENAME", edtFileName.text.toString() + ".note")
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
        checkIfUpToDate()
    }
    private fun login(){
        Intent(this, LoginActivity::class.java).also{
            startActivity(it)
        }
    }
    private fun showFiles() {
        if(checkForInternet(this) && isNotUpToDate){
            pullFilesFromServer()
        }else{
            pullFilesFromLocalMachine()
        }

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
    private fun checkIfUpToDate() = CoroutineScope(Dispatchers.IO).launch{
        var localTime = ""
        var serverTime = ""
        val querySnapshot = usersRef.get().await()
        for(document in querySnapshot.documents){
            serverTime = document.get("lastTime").toString()
        }
        try {
            val fileInputStream = openFileInput("")
            val inputReader = InputStreamReader(fileInputStream)
            localTime = inputReader.readText()
        }catch(e: Exception){
            localTime = " "
            isNotUpToDate = true
        }

        try{
            if(serverTime.substring(6,10).toInt() > localTime.substring(6,10).toInt()){
                isNotUpToDate = true
            }else if(serverTime.substring(3,5).toInt() > localTime.substring(3,5).toInt()){
                isNotUpToDate = true
            }else if(serverTime.substring(0,2).toInt() > localTime.substring(0,2).toInt()){
                isNotUpToDate = true
            }else if(serverTime.substring(11, 13).toInt() > localTime.substring(11, 13).toInt()){
                isNotUpToDate = true
            }else  if(serverTime.substring(14, 16).toInt() > localTime.substring(14, 16).toInt()){
                isNotUpToDate = true
            }else  if(serverTime.substring(17, 19).toInt() > localTime.substring(17, 19).toInt()){
                isNotUpToDate = true
            }else{
                isNotUpToDate = false
            }
        }catch(e: Exception){
            isNotUpToDate = false
            finish()
        }
    }//    "dd/MM/yyyy hh:mm:ss"
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
    private fun pullFilesFromServer(){

    }
    private fun pullFilesFromLocalMachine(){
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
}