package com.example.campusq.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

/** Explicit connection container. Never created by MainActivity, ViewModel or demo navigation. */
class FirebaseBackend internal constructor(
    val app: FirebaseApp,
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val functions: FirebaseFunctions
) {
    val serviceCatalog = FirebaseServiceCatalogRepository(firestore)

    companion object {
        /** Requires real Google Services resources; absence is an error, never a demo fallback. */
        fun fromConfiguration(context: Context, databaseId: String, functionsRegion: String): FirebaseBackend {
            require(databaseId.isNotBlank() && functionsRegion.isNotBlank())
            val app = requireNotNull(FirebaseApp.initializeApp(context.applicationContext)) {
                "Firebase is not configured. Supply genuine google-services.json and enable the Gradle opt-in."
            }
            require(!app.options.projectId.isNullOrBlank() && !app.options.projectId!!.startsWith("demo-")) {
                "Production configuration must identify a real Firebase project."
            }
            return FirebaseBackend(app, FirebaseAuth.getInstance(app),
                FirebaseFirestore.getInstance(app, databaseId), FirebaseFunctions.getInstance(app, functionsRegion))
        }
    }
}
