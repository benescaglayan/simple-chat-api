package com.caglayan.api.chat.util

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class Crypto {

    @Value("\${app.encryptionPassword}")
    fun setEncryptionKey(key: String) {
        ENCRYPTION_KEY = key
    }

    @PostConstruct
    fun init() {
        initialize()
    }

    companion object {

        private const val ALGORITHM = "PBEWITHSHA1ANDDESEDE"

        private var ENCRYPTION_KEY: String? = null

        private var encryptor = StandardPBEStringEncryptor()

        fun encrypt(data: String) = encryptor.encrypt(data)

        fun decrypt(data: String) = encryptor.decrypt(data)

        private fun initialize() {
            if (!encryptor.isInitialized) {
                encryptor.setPassword(ENCRYPTION_KEY)
                encryptor.setAlgorithm(ALGORITHM)
                encryptor.initialize()
            }
        }
    }
}