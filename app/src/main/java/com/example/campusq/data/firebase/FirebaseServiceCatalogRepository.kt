package com.example.campusq.data.firebase

import com.example.campusq.data.model.RegistrarService
import com.example.campusq.data.repository.ServiceCatalogRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/** Read-only, one-shot foundation. Closed Phase 3 rules intentionally reject this request. */
class FirebaseServiceCatalogRepository(private val firestore: FirebaseFirestore) : ServiceCatalogRepository {
    override suspend fun fetchServices(): List<RegistrarService> {
        val snapshot = suspendCoroutine { continuation ->
            firestore.collection("services").orderBy("name").limit(100).get(Source.SERVER)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        return snapshot.documents.map { ServiceDocumentMapper.fromDocument(it.id, it.data.orEmpty()) }
    }
}
