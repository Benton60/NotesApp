package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val btnCreateUser = findViewById<Button>(R.id.btnCreateUser)

        btnBack.setOnClickListener {
            finish()
        }

        btnCheck.setOnClickListener {
            checkUserLoginInfo()
        }

        btnCreateUser.setOnClickListener {
            Intent(this, CreateUserActivity::class.java).also{
                startActivity(it)
            }
        }
    }
    private fun checkUserLoginInfo() = CoroutineScope(Dispatchers.IO).launch{
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val usersRef = Firebase.firestore.collection("users")

        val user = usersRef
            .whereEqualTo("name", edtUsername.text.toString())
            .whereEqualTo("password", edtPassword.text.toString())
            .get()
            .await()
        if(user.isEmpty){
            edtPassword.setText("")
            withContext(Dispatchers.Main){
                Toast.makeText(this@LoginActivity, "Invalid Password", Toast.LENGTH_LONG).show()
            }
        }else{
            saveThePreviousUsersNotes()
        }
    }
    private fun saveLogin(){
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        try{
            val fileOutputStream = openFileOutput("userPassword.usr", MODE_PRIVATE)
            val outputWriter = OutputStreamWriter(fileOutputStream)
            outputWriter.write(edtPassword.text.toString())
            outputWriter.close()

            val fileOutputStream3 = openFileOutput("userLastUsed.sys", MODE_PRIVATE)
            val outputWriter3 = OutputStreamWriter(fileOutputStream3)
            outputWriter3.write(loadThisNote("userUsername.usr"))
            outputWriter3.close()

            val fileOutputStream2 = openFileOutput("userUsername.usr", MODE_PRIVATE)
            val outputWriter2 = OutputStreamWriter(fileOutputStream2)
            outputWriter2.write(edtUsername.text.toString())
            outputWriter2.close()
        }catch(e: IOException){
            println(e)
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
    private fun saveThePreviousUsersNotes() = CoroutineScope(Dispatchers.IO).launch{
        val usersRef = Firebase.firestore.collection("users")
        withContext(Dispatchers.Main) {
            if (checkForInternet(this@LoginActivity) && loadThisNote("userPassword.usr") != "") {
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
        deleteFile("lastTime.tim")
        saveLogin()
        finish()
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