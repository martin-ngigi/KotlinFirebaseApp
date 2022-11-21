package com.example.kotlinfirebaseapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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

        /**
         * to log out
         */
        //firebaseAuth.signOut()

        btnRegister.setOnClickListener{
            registerUser()
        }

        btnLogin.setOnClickListener{
            loginUser()
        }

        btnUpdateProfile.setOnClickListener{
            updateProfile()
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

    private fun updateProfile(){
        firebaseAuth.currentUser?.let { user ->
            val username = etUsername.text.toString()
            val photoURI = Uri.parse("android.resource://$packageName/${R.drawable.android3}")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(photoURI)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    /**
                     * check if user is logged in or not
                     */
                    checkLoggedInState()

                    user.updateProfile(profileUpdates).await()
                    /**
                     * Switching to main activity since it cant be accessed here
                     */
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "Successfully updated user profile ", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception){
                    /**
                     * Switching to main activity since it cant be accessed here
                     */
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "Error: ${e.message} ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    private fun checkLoggedInState() {
        val user = firebaseAuth.currentUser
        if (user == null){
            tvLoggedIn.text = "You are not logged in "
        }
        else{
            /**
             * set data to ui
             */
            tvLoggedIn.text = "You are logged in "
            etUsername.setText(user.displayName)
            ivProfilePicture.setImageURI(user.photoUrl)
        }
    }
}

