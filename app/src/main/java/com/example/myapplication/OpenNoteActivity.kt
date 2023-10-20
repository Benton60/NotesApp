package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
import java.text.SimpleDateFormat
import java.util.Date

class OpenNoteActivity : AppCompatActivity() {
    private val usersRef = Firebase.firestore.collection("users")
    private var currentUser = User(" ", " ", hashMapOf("" to ""), " ")
    private var userIsLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        updateUserInfo()

        setText(loadThisNote(getNoteName()))

        btnSave.setOnClickListener {

            if(saveThisNote(getNoteName())){
                updateUserInfo()
                updateDataToFireStore()
            }else{
                Toast.makeText(this, "Could Not Save File", Toast.LENGTH_LONG)
            }
        }

        btnDelete.setOnClickListener {
            deleteThisNote(getNoteName())
            updateUserInfo()
            updateDataToFireStore()
            finish()
        }
    }
    private fun updateDataToFireStore() = CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            if (checkForInternet(this@OpenNoteActivity) && userIsLoggedIn) {
                val user = usersRef
                    .whereEqualTo("name", currentUser.name)
                    .get()
                    .await()
                if (!user.isEmpty) {
                    for (document in user) {
                        usersRef.document(document.id).update(mapOf(
                            "notes" to FieldValue.delete()
                        ))
                        usersRef.document(document.id).set(
                            currentUser.getHashMapOf(),
                            SetOptions.merge()
                        ).await()
                    }
                } else {
                    val db = Firebase.firestore
                    db.collection("users")
                        .add(currentUser.getHashMapOf())
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@OpenNoteActivity,
                        "This file will only save locally until you connect to a network and connect your account",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            finish()
        }
    }
    private fun getNoteName(): String {
         return intent.getStringExtra("EXTRA_FILENAME").toString()
    }
    private fun getText(): String {
        val edtNote = findViewById<EditText>(R.id.edtNote)
        return edtNote.text.toString()
    }
    private fun setText(note: String?){
        val edtNote = findViewById<EditText>(R.id.edtNote)
        edtNote.setText(note, TextView.BufferType.EDITABLE)
    }
    private fun saveThisNote(noteName: String): Boolean{
        return try{
            val timeFileStream = openFileOutput("lastTime.tim", MODE_PRIVATE)
            val timeOutputWriter = OutputStreamWriter(timeFileStream)
            timeOutputWriter.write(SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(Date()))
            timeOutputWriter.close()

            val fileOutputStream = openFileOutput(noteName, MODE_PRIVATE)
            val outputWriter = OutputStreamWriter(fileOutputStream)
            outputWriter.write(getText())
            outputWriter.close()
            true
        }catch(e: IOException){
            println(e)
            false
        }
    }
    private fun deleteThisNote(noteName: String): Boolean{
        return try{
            deleteFile(noteName)
            true
        }catch(e: Exception){
            e.printStackTrace()
            false
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
    private fun updateUserInfo(){
        checkUserLoginInfo()
        currentUser.name = loadThisNote("userUsername.usr")
        currentUser.password = loadThisNote("userPassword.usr")

        val notesToAddToUser = hashMapOf<String, Any>(
            " " to " "
        )
        filesDir.listFiles()?.filter {
            it.canRead()
            it.isFile
            it.canWrite()
            it.exists()
            it.extension == "note"
        }?.forEach {
            notesToAddToUser[it.name] = loadThisNote(it.name)
        }
        notesToAddToUser.remove(" ")
        currentUser.notes = notesToAddToUser
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
    private fun checkUserLoginInfo() = CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            if (checkForInternet(this@OpenNoteActivity)) {
                val usersRef = Firebase.firestore.collection("users")
                val user = usersRef
                    .whereEqualTo("name", currentUser.name)
                    .whereEqualTo("password", currentUser.password)
                    .get()
                    .await()
                if (user.isEmpty) {
                    userIsLoggedIn = false
                    try {
                        deleteFile("userPassword.usr")
                        deleteFile("userUsername.usr")
                    } catch (e: Exception) {
                        print("e")
                    }
                } else {
                    userIsLoggedIn = true
                }
            } else {
                userIsLoggedIn = false
            }
        }
    }
}