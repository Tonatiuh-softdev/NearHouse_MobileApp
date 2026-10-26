package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.domain.repository.AdminListingRepository
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import com.equipo4.nearhome.domain.repository.ReportRepository
import com.equipo4.nearhome.domain.repository.UserRepository

object Repositories {
    val propiedades: PropiedadRepository by lazy { PropiedadRepositoryFake() }
    val reportes: ReportRepository by lazy { ReportRepositoryFake() }
    val anuncios: AdminListingRepository by lazy { AdminListingRepositoryFake() }
    val users: UserRepository by lazy { UserRepositoryFake() }
}
