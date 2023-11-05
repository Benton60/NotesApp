package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {
    var serverTime = ""
    private val usersRef = Firebase.firestore.collection("users")
    private var isfinished = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pullServerTime()
        val edtFileName = findViewById<EditText>(R.id.edtFileName)
        val btnBigBoi = findViewById<Button>(R.id.btnBigBoi)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val ltvFilesList = findViewById<ListView>(R.id.ltvFilesList)

        btnSettings.setOnClickListener {
            Intent(this, SettingsActivity::class.java).also{
                startActivity(it)
            }
        }
        checkIfLoggedIn()
        btnLogin.setOnClickListener {
            login()
        }
        edtFileName.setOnClickListener{
            showFiles()
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
        showFiles()
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
    private fun showFiles(){
        //var btnbigboi = findViewById<Button>(R.id.btnBigBoi)
        //btnbigboi.text = isServerTimeHigher().toString()
        pullServerTime()
        if(checkForInternet(this) && (isServerTimeHigher() || loadThisNote("userLastUsed.sys") != loadThisNote("userUsername.usr"))){
            pullFilesFromServer()
        }else{
            userIsAhead()
        }
        pullFilesFromLocalMachine()
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
    private fun pullFilesFromLocalMachine(){
        val edtFileName = findViewById<EditText>(R.id.edtFileName)
        val ltvFilesList = findViewById<ListView>(R.id.ltvFilesList)

        edtFileName.setText("")
        var filesList = mutableListOf<String>()

        filesDir.listFiles()?.filter {
            it.canRead()
            it.isFile
            it.canWrite()
            it.exists()
            it.extension == "note"
        }?.forEach {
            filesList.add(it.nameWithoutExtension)
        }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filesList)
        ltvFilesList.adapter = arrayAdapter
    }
    private fun pullFilesFromServer() = CoroutineScope(Dispatchers.IO).launch{
        try{
            val user = usersRef
                .whereEqualTo("name", loadThisNote("userUsername.usr"))
                .get()
                .await()
            filesDir.listFiles().filter{
                it.isFile
                it.exists()
                it.extension == "note"
            }.forEach{
                deleteFile(it.name)
            }
            for(user in user.documents){

                val notesFromServer = user.get("notes") as HashMap<String, Any>

                notesFromServer.forEach{
                    saveThisNote(it.key, it.value.toString())
                }
            }
        }catch(e: Exception){
            var btn = findViewById<Button>(R.id.btnBigBoi)
            btn.text = "Error"
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
    private fun pullServerTime() = CoroutineScope(Dispatchers.IO).launch{
        try {
            val user = usersRef
                .whereEqualTo("name", loadThisNote("userUsername.usr"))
                .get()
                .await()
            for (document in user.documents) {
                serverTime = document.get("lastTime").toString()
            }
        }catch(e: Exception){
            serverTime = "Not Signed In"
        }
        isfinished = true
    }
    private fun isServerTimeHigher(): Boolean{
        var localTime = "00000000000000000000"
        filesDir.listFiles().filter{
            it.extension == "tim"
        }.forEach{
            localTime = loadThisNote(it.name)
        }
        try{
            if(serverTime.substring(6,10).toInt() > localTime.substring(6,10).toInt()){
                return true
            }else if(serverTime.substring(3,5).toInt() > localTime.substring(3,5).toInt()){
                return true
            }else if(serverTime.substring(0,2).toInt() > localTime.substring(0,2).toInt()){
                return true
            }else if(serverTime.substring(11, 13).toInt() > localTime.substring(11, 13).toInt()){
                return true
            }else  if(serverTime.substring(14, 16).toInt() > localTime.substring(14, 16).toInt()){
                return true
            }else  if(serverTime.substring(17, 19).toInt() > localTime.substring(17, 19).toInt()){
                return true
            }else{
                return false
            }
        }catch(e: Exception){
            return false
        }
    }
    private fun saveThisNote(note: String, contents: String){
        try{
            val timeFileStream = openFileOutput("lastTime.tim", MODE_PRIVATE)
            val timeOutputWriter = OutputStreamWriter(timeFileStream)
            timeOutputWriter.write(serverTime)
            timeOutputWriter.close()

            val fileOutputStream = openFileOutput(note, MODE_PRIVATE)
            val outputWriter = OutputStreamWriter(fileOutputStream)
            outputWriter.write(contents)
            outputWriter.close()
        }catch(e: IOException){
            println(e)
        }
    }
    private fun userIsAhead() = CoroutineScope(Dispatchers.IO).launch{
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        withContext(Dispatchers.Main) {
            if ((checkForInternet(this@MainActivity) && btnLogin.text == "Signed In") && (loadThisNote("userLastUsed.sys") == loadThisNote("userUsername.usr"))) {
                val user = usersRef
                    .whereEqualTo("name", loadThisNote("userUsername.usr"))
                    .get()
                    .await()
                if (!user.isEmpty) {
                    for (document in user) {
                        usersRef.document(document.id).update(mapOf(
                            "notes" to FieldValue.delete()
                        ))
                        usersRef.document(document.id).set(
                            hashMapOf<String, Any>(
                                "notes" to getHashMapOfNotes()
                            ),
                            SetOptions.merge()
                        ).await()
                    }
                }
            }
        }
    }
    private fun getHashMapOfNotes(): HashMap<String, Any>{
        var notes = hashMapOf<String, Any>(
            " " to " "
        )
        filesDir.listFiles()?.filter {
            it.canRead()
            it.isFile
            it.canWrite()
            it.exists()
            it.extension == "note"
        }?.forEach {
            notes[it.name] = loadThisNote(it.name)
        }
        notes.remove(" ")
        return notes
    }
}


