package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.domain.repository.PropiedadRepository
import com.equipo4.nearhome.domain.repository.ReportRepository
import com.equipo4.nearhome.domain.repository.UserRepository

/**
 * Punto único para elegir la implementación. Al tener backend, cambiar SOLO esta línea
 * (ej. PropiedadRepositoryRemote()); ni la pantalla ni el ViewModel se modifican.
 */
object Repositories {
    val propiedades: PropiedadRepository by lazy { PropiedadRepositoryFake() }
    val reportes: ReportRepository by lazy { ReportRepositoryFake() }
    val users: UserRepository by lazy { UserRepositoryFake() }
}
