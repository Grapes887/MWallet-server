package com.example.data.firebase

import com.example.domain.exception.ApiException
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.server.config.ApplicationConfig
import java.io.File
import java.io.FileInputStream

object FirebaseService {

    private var initialized = false

    fun init(config: ApplicationConfig) {
        if (initialized) return

        val credentialsPath = config.propertyOrNull("firebase.credentials")?.getString()
            ?: System.getenv("FIREBASE_CREDENTIALS")
            ?: throw ApiException(
                500,
                "Укажите firebase.credentials в application.conf или переменную FIREBASE_CREDENTIALS"
            )

        val credentialsFile = File(credentialsPath)
        if (!credentialsFile.exists()) {
            throw ApiException(500, "Файл Firebase credentials не найден: $credentialsPath")
        }

        FileInputStream(credentialsFile).use { stream ->
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
        }
        initialized = true
    }

    fun verifyIdToken(idToken: String): FirebaseToken {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(idToken)
        } catch (e: Exception) {
            throw ApiException(401, "Недействительный Firebase token")
        }
    }
}
