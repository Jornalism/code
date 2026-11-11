package com.example.campusq.data.demo

import com.example.campusq.data.repository.CampusQStateSource
import com.example.campusq.data.repository.ServiceCatalogRepository
import com.example.campusq.data.model.RegistrarService

/** Same read boundary as Firebase, backed by the current shared demo state. */
class DemoServiceCatalogRepository(private val state: CampusQStateSource) : ServiceCatalogRepository {
    override suspend fun fetchServices(): List<RegistrarService> = state.services.value
}
