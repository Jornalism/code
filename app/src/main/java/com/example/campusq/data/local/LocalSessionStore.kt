package com.example.campusq.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Only encrypted bearer credentials are persisted, bound to one server origin. No passwords. */
class LocalSessionStore(context: Context, private val origin: String) {
    private val preferences = context.applicationContext.getSharedPreferences("campusq_local_session", Context.MODE_PRIVATE)
    private val alias = "campusq.local.session.v1"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
        }.generateKey()
    }
    @Synchronized fun read(): String? = try {
        if (preferences.getString("origin", null) != origin) null else {
            val iv = preferences.getString("iv", null)
            val data = preferences.getString("ciphertext", null)
            if (iv == null || data == null) null else Cipher.getInstance("AES/GCM/NoPadding").run {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
                updateAAD(origin.toByteArray(Charsets.UTF_8))
                doFinal(Base64.decode(data, Base64.NO_WRAP)).toString(Charsets.UTF_8)
            }
        }
    } catch (_: Exception) { clear(); null }
    @Synchronized fun save(token: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(origin.toByteArray(Charsets.UTF_8))
        val encoded = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        check(preferences.edit().putString("origin", origin)
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString("ciphertext", Base64.encodeToString(encoded, Base64.NO_WRAP)).commit()) { "Session could not be saved." }
    }
    @Synchronized fun clear() {
        val removed = preferences.edit().clear().commit()
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); if (containsAlias(alias)) deleteEntry(alias) }
        check(removed) { "Session storage could not be cleared." }
    }
}
