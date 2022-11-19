package com.example.kotlinfirebaseapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener{
            registerUser()
        }

        btnLogin.setOnClickListener{
            loginUser()
        }


    }

    private fun registerUser(){
        val email = etEmailRegister.text.toString()
        val password = etPasswordRegister.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await() //everything went on well
                    withContext(Dispatchers.Main){
                        checkLoggedInState()
                    }
                }
                catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "Error: ${e.message} ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loginUser(){
        val email = etEmailLogin.text.toString()
        val password = etPasswordLogin.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firebaseAuth.signInWithEmailAndPassword(email, password).await() //everything went on well
                    withContext(Dispatchers.Main){
                        checkLoggedInState()
                    }
                }
                catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "Error: ${e.message} ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkLoggedInState() {
        if (firebaseAuth.currentUser == null){
            tvLoggedIn.text = "You are not logged in "
        }
        else{
            tvLoggedIn.text = "You are logged in "
        }
    }
}

