package com.example.campusq.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.functions.FirebaseFunctions

/** Debug source set only. Explicit Firebase demo project and Android emulator host. */
object LocalFirebaseBackend {
    const val PROJECT_ID = "demo-campusq-local"

    fun create(context: Context): FirebaseBackend {
        val app = FirebaseApp.initializeApp(context.applicationContext, FirebaseOptions.Builder()
            .setProjectId(PROJECT_ID)
            .setApplicationId("1:000000000000:android:local")
            .setApiKey("local-emulator-only")
            .build(), "campusq-local")
        val auth = FirebaseAuth.getInstance(app).also { it.useEmulator("10.0.2.2", 9099) }
        val firestore = FirebaseFirestore.getInstance(app).also {
            it.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build()
            it.useEmulator("10.0.2.2", 8080)
        }
        val functions = FirebaseFunctions.getInstance(app, "asia-southeast1").also {
            it.useEmulator("10.0.2.2", 5001)
        }
        return FirebaseBackend(app, auth, firestore, functions)
    }
}
