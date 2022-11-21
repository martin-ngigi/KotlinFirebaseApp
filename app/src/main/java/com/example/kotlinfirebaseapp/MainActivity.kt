package com.example.kotlinfirebaseapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val REQUEST_CODE_SIGN_IN=0

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

        btnGoogleSignIn.setOnClickListener{
            googleSignIn()
        }

        btnLogOut.setOnClickListener{
            logOut()
        }

    }

    private fun logOut() {
        firebaseAuth.signOut()
        Toast.makeText(this@MainActivity, "Successfully logged out.", Toast.LENGTH_SHORT).show()


    }

    private fun googleSignIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val signInClient = GoogleSignIn.getClient(this, options)

        /**
         * show popup
         */
        signInClient.signInIntent.also {
            startActivityForResult(it, REQUEST_CODE_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SIGN_IN){
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                googleAuthForFirebase(it)
            }
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firebaseAuth.signInWithCredential(credentials).await()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "Successfully logged in.\nEmail: ${account.email.toString()}", Toast.LENGTH_SHORT).show()

                    /**
                     * set data to ui
                     */
                    tvLoggedIn.text = "You are logged in "
                    etUsername.setText(account.displayName)
                    ivProfilePicture.setImageURI(account.photoUrl)
                }
            }
            catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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

