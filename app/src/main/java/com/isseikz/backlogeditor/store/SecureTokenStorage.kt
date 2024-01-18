package com.isseikz.backlogeditor.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.preference.PreferenceManager
import timber.log.Timber
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SecureTokenStorage(context: Context) {
    private val defaultSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun storeAccessToken(token: String) {
        defaultSharedPreferences.edit().putString("personal_access_token", token).apply()
    }

    fun getAccessToken(): String? {
        return defaultSharedPreferences.getString("personal_access_token", null)
    }

    private fun encrypt(input: String): String {
        val c = Cipher.getInstance("AES")
        val keySpec = SecretKeySpec("TODO".toByteArray(), "AES")
        c.init(Cipher.ENCRYPT_MODE, keySpec)
        return Base64.encodeToString(c.doFinal(input.toByteArray()), Base64.DEFAULT)
    }

    private fun decrypt(input: String): String {
        val c = Cipher.getInstance("AES")
        val keySpec = SecretKeySpec("TODO".toByteArray(), "AES")
        c.init(Cipher.DECRYPT_MODE, keySpec)
        return String(c.doFinal(Base64.decode(input, Base64.DEFAULT)))
    }
}
