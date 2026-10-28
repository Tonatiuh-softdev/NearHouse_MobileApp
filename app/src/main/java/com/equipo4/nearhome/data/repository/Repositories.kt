package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.domain.repository.AdminListingRepository
import com.equipo4.nearhome.domain.repository.AdminListingRepositoryApi
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import com.equipo4.nearhome.domain.repository.ReportRepository
import com.equipo4.nearhome.domain.repository.UserRepository

object Repositories {
    private var appContext: Context? = null

    /** Se llama una vez al abrir la app (MainActivity), antes de usar cualquier repositorio con API. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun ctx(): Context = requireNotNull(appContext) {
        "Falta llamar a Repositories.init(applicationContext) en MainActivity"
    }

    val propiedades: PropiedadRepository by lazy { PropiedadRepositoryApi(ctx()) }
    val reportes: ReportRepository by lazy { ReportRepositoryFake() }
    val anuncios: AdminListingRepository by lazy { AdminListingRepositoryApi(ctx()) }
    val users: UserRepository by lazy { UserRepositoryFake() }
}