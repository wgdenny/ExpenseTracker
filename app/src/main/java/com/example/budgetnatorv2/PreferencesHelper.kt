package com.example.budgetnatorv2

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


class PreferencesHelper(context: Context) {
    private val PREFS_NAME = "com.example.budgetnatorv2.prefs"
    private val USERNAME = "username"
    private val PASSWORD = "password"
    private var loggedIn = false
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(username: String, password: String) {
        val editor = prefs.edit()
        editor.putString(USERNAME, username)
        editor.putString(PASSWORD, password)
        editor.apply()
    }

    fun getUsername(): String? {
        return prefs.getString(USERNAME, null)
    }

    fun getPassword(): String? {
        return prefs.getString(PASSWORD, null)
    }
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("loggedIn", loggedIn)
    }
    fun setLoggedIn(loggedIn: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean("loggedIn", loggedIn)
        editor.apply()
    }
}