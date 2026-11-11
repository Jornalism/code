package com.example.campusq.data.repository

import com.example.campusq.data.model.RegistrarService

/** Future asynchronous boundary; errors propagate instead of silently falling back to demo data. */
interface ServiceCatalogRepository {
    suspend fun fetchServices(): List<RegistrarService>
}
