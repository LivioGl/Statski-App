package com.example.statski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract())
    { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(res: FirebaseAuthUIAuthenticationResult?) {
        val response = res?.idpResponse
        if(res?.resultCode == RESULT_OK){
            val user = FirebaseAuth.getInstance().currentUser
            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        } else
        {
            Log.e("Statski","Login Error!")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )
//        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)

    }
}