package com.isseikz.backlogeditor.store

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.auth.CredentialGitHub

class SecureTokenStorage(private val context: Context) : PreferenceDataStore() {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getAccessToken(): String? = context.getString(R.string.preference_key_github_pat)
        .let { key ->
            sharedPreferences.getString(key, null)
        }

    fun getCredential(): CredentialGitHub? {
        return getString("github_username", null)
            ?.let { it to getString("personal_access_token", null) }
            ?.let { (username, token) ->
                if (token != null) {
                    CredentialGitHub(username, token)
                } else {
                    null
                }
            }
    }

    override fun putString(key: String?, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String?, defValue: String?): String? {
        return sharedPreferences.getString(key, defValue)
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        sharedPreferences.edit().putStringSet(key, values).apply()
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return sharedPreferences.getStringSet(key, defValues)
    }
}
