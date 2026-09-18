package com.example.invisibleauthenticationsystem.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val cipher = Cipher.getInstance(TRANSFORMATION)

    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        outputStream.use {
            it.write(iv)
            val encryptedBytes = cipher.doFinal(bytes)
            it.write(encryptedBytes)
        }
        return iv
    }

    fun decrypt(inputStream: InputStream): ByteArray {
        return inputStream.use {
            val iv = ByteArray(12)
            it.read(iv)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
            val encryptedBytes = it.readBytes()
            cipher.doFinal(encryptedBytes)
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return keyGenerator.generateKey()
    }

    companion object {
        private const val ALIAS = "VaultKeyAlias"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
