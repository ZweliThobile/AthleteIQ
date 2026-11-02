package com.example.athleteiq.data

import android.content.Context
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class LocalAccount(val email: String, val salt: String, val passwordHash: String, val profile: AthleteProfile)

interface AccountStore {
    fun find(email: String): LocalAccount?
    fun save(account: LocalAccount): Boolean
}

class MemoryAccountStore : AccountStore {
    private val accounts = mutableMapOf<String, LocalAccount>()
    override fun find(email: String) = accounts[email]
    override fun save(account: LocalAccount): Boolean { accounts[account.email] = account; return true }
}

/** App-private local accounts. Passwords are salted hashes, never plaintext. */
class DeviceAccountStore(context: Context) : AccountStore {
    private val preferences = context.applicationContext.getSharedPreferences("athlete_accounts", Context.MODE_PRIVATE)
    override fun find(email: String): LocalAccount? {
        val raw = preferences.getString(email, null) ?: return null
        val json = JSONObject(raw)
        return LocalAccount(email, json.getString("salt"), json.getString("hash"), AthleteProfile(
            json.getString("name"), json.getString("age"), json.getString("sport"), json.getString("hours"), json.optString("weightKg", ""), json.optString("heightCm", "")))
    }
    override fun save(account: LocalAccount): Boolean {
        val json = JSONObject().put("salt", account.salt).put("hash", account.passwordHash)
            .put("name", account.profile.name).put("age", account.profile.age)
            .put("sport", account.profile.sport).put("hours", account.profile.weeklyHours)
            .put("weightKg", account.profile.weightKg).put("heightCm", account.profile.heightCm)
        return preferences.edit().putString(account.email, json.toString()).commit()
    }
}

object AccountPasswords {
    fun normalize(email: String) = email.trim().lowercase(Locale.ROOT)
    fun salt(): String = SecureRandom().generateSeed(16).joinToString("") { "%02x".format(it) }
    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(Charsets.UTF_8), 210_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
                .joinToString("") { "%02x".format(it) }
        } finally { spec.clearPassword() }
    }
    fun matches(password: String, account: LocalAccount) = MessageDigest.isEqual(
        hash(password, account.salt).toByteArray(Charsets.UTF_8), account.passwordHash.toByteArray(Charsets.UTF_8))
}
