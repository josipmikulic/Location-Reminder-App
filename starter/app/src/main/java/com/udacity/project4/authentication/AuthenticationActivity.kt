package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        loginButton = findViewById<Button>(R.id.button_login).apply {
            isEnabled = false
            setOnClickListener {
                startSignIn()
            }
        }

        checkIfUserLoggedIn()
    }

    private fun checkIfUserLoggedIn() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            navigateToRemindersActivity()
        } else {
            loginButton.isEnabled = true
        }
    }

    private fun startSignIn() {
        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
            listOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().build(),
            )
        ).setTheme(R.style.AppTheme).build()

        signInLauncher.launch(signInIntent)
    }

    private fun navigateToRemindersActivity() {
        val intent = Intent(
            this,
            RemindersActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
    }

    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()

    ) { result ->
        val response = result.idpResponse

        if (result.resultCode == RESULT_OK) {
            navigateToRemindersActivity()
        } else {
            var errorText = if (response == null) {
                getString(R.string.error_sign_in_cancelled)
            } else {
                if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    getString(R.string.error_no_internet)
                } else {
                    getString(R.string.error_unknown)
                }
            }

            Toast.makeText(
                this,
                errorText,
                Toast.LENGTH_SHORT
            )
        }
    }
}
