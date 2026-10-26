package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.Advertiser
import com.equipo4.nearhome.domain.model.DeactivationInfo
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.domain.model.StatusChange
import com.equipo4.nearhome.domain.repository.AdminListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

/** Datos de prueba en memoria: al cerrar la app vuelven a como estaban. */
class AdminListingRepositoryFake : AdminListingRepository {

    private val _listings = MutableStateFlow(seed())
    override val listings: StateFlow<List<AdminListing>> = _listings.asStateFlow()

    override fun getById(id: String): AdminListing? = _listings.value.firstOrNull { it.id == id }

    override fun approve(id: String, actor: String) =
        change(id, ListingStatus.PENDIENTE, ListingStatus.ACTIVO, actor, null)

    override fun reject(id: String, reason: String, actor: String) =
        change(id, ListingStatus.PENDIENTE, ListingStatus.RECHAZADO, actor, reason.trim())

    override fun deactivate(id: String, cause: String, actor: String) =
        change(id, ListingStatus.ACTIVO, ListingStatus.DESACTIVADO, actor, cause.trim())

    override fun reactivate(id: String, actor: String) =
        change(id, ListingStatus.DESACTIVADO, ListingStatus.ACTIVO, actor, null)

    override fun delete(id: String) {
        _listings.update { list -> list.filterNot { it.id == id } }
    }

    /** Aplica un cambio de estado solo si el anuncio está en el estado esperado. */
    private fun change(id: String, expected: ListingStatus, to: ListingStatus, actor: String, reason: String?) {
        val now = System.currentTimeMillis()
        _listings.update { list ->
            list.map { l ->
                if (l.id != id || l.status != expected) l
                else l.copy(
                    status = to,
                    history = l.history + StatusChange(expected, to, now, actor, reason),
                    deactivation = if (to == ListingStatus.DESACTIVADO) {
                        DeactivationInfo(cause = reason.orEmpty(), timestamp = now, actor = actor)
                    } else null
                )
            }
        }
    }

    companion object {
        private const val ADMIN = "Admin NearHouse"

        private val mariana = Advertiser("Mariana López Hernández", "mariana.lopez@correo.com", verified = true)
        private val anthony = Advertiser("Anthony Edward Stark", "anthony.stark@correo.com", verified = true)
        private val carlos = Advertiser("Carlos Méndez Ortega", "carlos.mendez@correo.com", verified = false)
        private val sofia = Advertiser("Sofía Ramírez Cruz", "sofia.ramirez@correo.com", verified = true)
        private val luis = Advertiser("Luis Fernando Gómez", "luis.gomez@correo.com", verified = false)

        private val deptoA = listOf(R.drawable.prop_depto_sala, R.drawable.prop_depto_cocina, R.drawable.prop_depto_recamara, R.drawable.prop_depto_terraza, R.drawable.prop_depto_fachada)
        private val deptoB = listOf(R.drawable.prop_depto_sala2, R.drawable.prop_depto_cocina2, R.drawable.prop_depto_recamara2, R.drawable.prop_depto_bano, R.drawable.prop_depto_alberca)
        private val casaA = listOf(R.drawable.prop_casa_fachada, R.drawable.prop_casa_sala, R.drawable.prop_casa_cocina, R.drawable.prop_casa_recamara, R.drawable.prop_casa_jardin)
        private val casaB = listOf(R.drawable.prop_casa_fachada2, R.drawable.prop_casa_sala2, R.drawable.prop_casa_cocina, R.drawable.prop_casa_cochera, R.drawable.prop_casa_jardin)
        private val terrenoA = listOf(R.drawable.prop_terreno_1, R.drawable.prop_terreno_4, R.drawable.prop_terreno_3)
        private val terrenoB = listOf(R.drawable.prop_terreno_2, R.drawable.prop_terreno_5)
        private val terrenoC = listOf(R.drawable.prop_terreno_5)

        private fun ts(month: Int, day: Int, hour: Int = 10, minute: Int = 0): Long =
            Calendar.getInstance().apply { clear(); set(2026, month - 1, day, hour, minute) }.timeInMillis

        private fun listing(
            id: String, title: String, type: PropertyType, mode: ListingType, price: Double,
            location: String, area: Int, beds: Int?, baths: Int?, garages: Int?,
            description: String, images: List<Int>, advertiser: Advertiser, registeredAt: Long,
            status: ListingStatus = ListingStatus.PENDIENTE,
            later: List<StatusChange> = emptyList(),
            deactivation: DeactivationInfo? = null
        ) = AdminListing(
            id = id, title = title, type = type, listingType = mode, price = price,
            location = location, areaSqM = area, bedrooms = beds, bathrooms = baths, garages = garages,
            description = description, images = images, advertiser = advertiser,
            registeredAt = registeredAt, status = status,
            history = listOf(StatusChange(null, ListingStatus.PENDIENTE, registeredAt, advertiser.name)) + later,
            deactivation = deactivation
        )

        private fun seed(): List<AdminListing> = listOf(
            // ---- Pendientes (la cola de revisión) ----
            listing("NH-8421", "Departamento amueblado cerca del centro", PropertyType.DEPARTAMENTO, ListingType.RENTA, 10090.0,
                "Av. Elías Zamora Verduzco, Manzanillo, Colima", 85, 2, 1, 1,
                "Departamento amueblado con cocina integrada, sala-comedor y balcón. Cerca de comercios y transporte.",
                deptoA, mariana, ts(10, 11, 9, 30)),
            listing("NH-8420", "Casa residencial en Real Vista", PropertyType.CASA, ListingType.VENTA, 4850000.0,
                "Col. Real Vista, Manzanillo, Colima", 210, 4, 3, 2,
                "Casa de dos niveles con jardín, cochera para dos autos y cuarto de servicio.",
                casaA, anthony, ts(10, 10, 16, 15)),
            listing("NH-8419", "Terreno plano en Salagua", PropertyType.TERRENO, ListingType.VENTA, 650000.0,
                "Col. Salagua, Manzanillo, Colima", 300, null, null, null,
                "Terreno plano de 10 x 30 metros, con servicios a pie de calle y escrituras en regla.",
                terrenoA, carlos, ts(10, 9, 12, 0)),
            listing("NH-8418", "Casa en renta cerca de la playa con alberca, jardín amplio y cochera techada para dos autos", PropertyType.CASA, ListingType.RENTA, 14000.0,
                "Col. Las Brisas, Manzanillo, Colima", 120, 2, 2, 1,
                "Casa a pocos minutos de la playa, con alberca compartida y área de asador.",
                casaB, sofia, ts(10, 8, 18, 40)),
            listing("NH-8417", "Departamento nuevo en Las Brisas", PropertyType.DEPARTAMENTO, ListingType.VENTA, 1890000.0,
                "Col. Las Brisas, Manzanillo, Colima", 95, 3, 2, 1,
                "Departamento a estrenar con acabados de lujo, alberca y gimnasio en el edificio.",
                deptoB, luis, ts(10, 7, 11, 5)),
            listing("NH-8416", "Terreno comercial sobre carretera", PropertyType.TERRENO, ListingType.RENTA, 8500.0,
                "Carretera Manzanillo–Colima km 4, Colima", 450, null, null, null,
                "Terreno con frente a carretera, ideal para bodega o local comercial.",
                terrenoB, mariana, ts(10, 6, 8, 20)),
            // ---- Activos ----
            listing("NH-8410", "Departamento con vista al mar", PropertyType.DEPARTAMENTO, ListingType.RENTA, 18500.0,
                "Av. Elías Zamora Verduzco, Manzanillo, Colima", 110, 3, 2, 2,
                "Departamento amueblado en piso alto con vista panorámica.",
                deptoA, anthony, ts(9, 28, 10, 0), ListingStatus.ACTIVO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(9, 29, 9, 0), ADMIN))),
            listing("NH-8409", "Casa de dos niveles en Real Vista", PropertyType.CASA, ListingType.VENTA, 2450000.0,
                "Col. Real Vista, Manzanillo, Colima", 160, 3, 2, 2,
                "Casa con sala amplia, cocina equipada y patio trasero.",
                casaA, sofia, ts(9, 25, 14, 30), ListingStatus.ACTIVO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(9, 26, 10, 0), ADMIN))),
            listing("NH-8408", "Terreno de 300 m² en Salagua", PropertyType.TERRENO, ListingType.VENTA, 1200000.0,
                "Col. Salagua, Manzanillo, Colima", 300, null, null, null,
                "Terreno esquina, listo para construir.",
                terrenoA, carlos, ts(9, 20, 9, 45), ListingStatus.ACTIVO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(9, 21, 12, 0), ADMIN))),
            listing("NH-8407", "Casa familiar en renta", PropertyType.CASA, ListingType.RENTA, 12000.0,
                "Col. Valle Dorado, Manzanillo, Colima", 130, 3, 2, 1,
                "Casa en privada con vigilancia, cerca de escuelas.",
                casaB, luis, ts(9, 18, 17, 10), ListingStatus.ACTIVO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(9, 19, 9, 30), ADMIN))),
            // ---- Rechazados ----
            listing("NH-8402", "Departamento en venta amueblado", PropertyType.DEPARTAMENTO, ListingType.VENTA, 890000.0,
                "Av. Elías Zamora Verduzco, Manzanillo, Colima", 85, 2, 1, 1,
                "Departamento con muebles incluidos.",
                deptoB, anthony, ts(9, 12, 13, 0), ListingStatus.RECHAZADO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.RECHAZADO, ts(9, 13, 11, 0), ADMIN,
                    "Las fotografías no corresponden a la propiedad anunciada."))),
            listing("NH-8401", "Casa en renta sin datos de contacto claros", PropertyType.CASA, ListingType.RENTA, 9500.0,
                "Col. Centro, Manzanillo, Colima", 90, 2, 1, 0,
                "Casa pequeña en el centro.",
                casaA, carlos, ts(9, 10, 15, 0), ListingStatus.RECHAZADO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.RECHAZADO, ts(9, 11, 10, 0), ADMIN,
                    "La descripción es insuficiente y el precio parece incorrecto."))),
            // ---- Desactivados ----
            listing("NH-8395", "Departamento económico en renta", PropertyType.DEPARTAMENTO, ListingType.RENTA, 7800.0,
                "Col. Fovissste, Manzanillo, Colima", 60, 1, 1, 0,
                "Departamento de una recámara, ideal para una persona o pareja.",
                deptoA, sofia, ts(8, 30, 9, 0), ListingStatus.DESACTIVADO,
                later = listOf(
                    StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(8, 31, 10, 0), ADMIN),
                    StatusChange(ListingStatus.ACTIVO, ListingStatus.DESACTIVADO, ts(9, 20, 16, 30), ADMIN,
                        "El anunciante solicitó pausar el anuncio.")),
                deactivation = DeactivationInfo("El anunciante solicitó pausar el anuncio.", ts(9, 20, 16, 30), ADMIN)),
            listing("NH-8390", "Casa en venta con cochera", PropertyType.CASA, ListingType.VENTA, 1980000.0,
                "Col. Salagua, Manzanillo, Colima", 140, 3, 2, 2,
                "Casa con cochera para dos autos y patio.",
                casaB, luis, ts(8, 25, 11, 30), ListingStatus.DESACTIVADO,
                later = listOf(
                    StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(8, 26, 9, 0), ADMIN),
                    StatusChange(ListingStatus.ACTIVO, ListingStatus.DESACTIVADO, ts(9, 15, 12, 0), ADMIN,
                        "Se detectó información de contacto duplicada.")),
                deactivation = DeactivationInfo("Se detectó información de contacto duplicada.", ts(9, 15, 12, 0), ADMIN)),
            // ---- Rentado / Vendido ----
            listing("NH-8380", "Departamento rentado en Salagua", PropertyType.DEPARTAMENTO, ListingType.RENTA, 12000.0,
                "Col. Salagua, Manzanillo, Colima", 75, 2, 1, 1,
                "Departamento que ya fue rentado.",
                deptoB, mariana, ts(8, 15, 10, 0), ListingStatus.RENTADO_VENDIDO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(8, 16, 9, 0), ADMIN)))
        ) + listOf(
            listing("NH-8415", "Terreno en esquina con una sola foto", PropertyType.TERRENO, ListingType.VENTA, 520000.0,
                "Col. Campos, Manzanillo, Colima", 200, null, null, null,
                "Terreno pequeño en esquina, con servicios.",
                terrenoC, anthony, ts(10, 5, 10, 0), ListingStatus.ACTIVO,
                later = listOf(StatusChange(ListingStatus.PENDIENTE, ListingStatus.ACTIVO, ts(10, 6, 9, 0), ADMIN)))
        )
    }
}