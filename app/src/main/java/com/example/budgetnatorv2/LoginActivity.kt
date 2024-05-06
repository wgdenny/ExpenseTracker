package com.example.budgetnatorv2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val register = findViewById<Button>(R.id.register)

        login.setOnClickListener {

            val prefsHelper = PreferencesHelper(this)
            if (username.text.toString() == prefsHelper.getUsername() && password.text.toString() == prefsHelper.getPassword()) {
                prefsHelper.setLoggedIn(true)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                prefsHelper.setLoggedIn(false)
                username.error = "Invalid username or password"
                password.error = "Invalid username or password"
            }
        }

        register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}