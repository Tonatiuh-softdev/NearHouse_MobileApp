package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Implementación de prueba: datos fijos en memoria (se reinician al cerrar la app). */
class PropiedadRepositoryFake : PropiedadRepository {

    private val properties = MutableStateFlow(
        listOf(
            Property(
                id = "1", title = "DEPARTAMENTO AMUEBLADO EN RENTA", price = 18500.0,
                listingType = ListingType.RENTA,
                location = "Av. Elías Zamora Verduzco, Las Hadas, Manzanillo",
                type = PropertyType.DEPARTAMENTO, areaSqM = 110,
                bedrooms = 3, bathrooms = 2, garages = 2,
                images = listOf(R.drawable.prop_depto_sala, R.drawable.prop_depto_cocina, R.drawable.prop_depto_recamara), isSaved = true,
                latitude = 19.0985, longitude = -104.3120,
                colonia = "Las Hadas", piso = 2, amueblado = true, mascotasPermitidas = true, elevador = true
            ),
            Property(
                id = "2", title = "DEPARTAMENTO EN VENTA CLUB SANTIAGO", price = 890000.0,
                listingType = ListingType.VENTA,
                location = "Club Santiago, Manzanillo, Colima",
                type = PropertyType.DEPARTAMENTO, areaSqM = 85,
                bedrooms = 2, bathrooms = 1, garages = 1,
                images = listOf(R.drawable.prop_depto_sala2, R.drawable.prop_depto_cocina2), isSaved = true,
                latitude = 19.0950, longitude = -104.3050,
                colonia = "Club Santiago", piso = 1, amueblado = false, elevador = false
            ),
            Property(
                id = "3", title = "CASA RESIDENCIAL MIRAMAR", price = 2450000.0,
                listingType = ListingType.VENTA,
                location = "Col. Miramar, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 160,
                bedrooms = 3, bathrooms = 2, garages = 2,
                images = listOf(R.drawable.prop_casa_fachada, R.drawable.prop_casa_sala), isSaved = false,
                latitude = 19.1210, longitude = -104.3450,
                colonia = "Miramar", niveles = 2, jardinPatio = true
            ),
            Property(
                id = "4", title = "CASA EN RENTA SALAHUA", price = 14000.0,
                listingType = ListingType.RENTA,
                location = "Col. Salahua, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 120,
                bedrooms = 2, bathrooms = 2, garages = 1,
                images = listOf(R.drawable.prop_casa_fachada2, R.drawable.prop_casa_sala2), isSaved = true,
                latitude = 19.1000, longitude = -104.3300,
                colonia = "Salahua", niveles = 1, jardinPatio = true, mascotasPermitidas = true
            ),
            Property(
                id = "5", title = "TERRENO PLANO EN VENTA LAS BRISAS", price = 650000.0,
                listingType = ListingType.VENTA,
                location = "Col. Las Brisas, Manzanillo, Colima",
                type = PropertyType.TERRENO, areaSqM = 300,
                frontMeters = 10.0, depthMeters = 30.0,
                images = listOf(R.drawable.prop_terreno_1, R.drawable.prop_terreno_4), isSaved = false,
                latitude = 19.1180, longitude = -104.3010,
                colonia = "Las Brisas", usoSuelo = "Habitacional", superficieSqM = 300.0
            ),
            Property(
                id = "6", title = "TERRENO COMERCIAL CENTRO HISTÓRICO", price = 1200000.0,
                listingType = ListingType.VENTA,
                location = "Centro Histórico, Manzanillo, Colima",
                type = PropertyType.TERRENO, areaSqM = 450,
                frontMeters = 15.0, depthMeters = 30.0,
                images = listOf(R.drawable.prop_terreno_5, R.drawable.prop_terreno_2), isSaved = false,
                latitude = 19.0830, longitude = -104.2980,
                colonia = "Centro Histórico", usoSuelo = "Habitacional", superficieSqM = 450.0
            ),
            Property(
                id = "7", title = "CASA EN RENTA SAN PEDRITO", price = 11000.0,
                listingType = ListingType.RENTA,
                location = "Col. San Pedrito, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 95,
                bedrooms = 2, bathrooms = 1, garages = 1,
                images = listOf(R.drawable.prop_casa_sala, R.drawable.prop_casa_cocina), isSaved = false,
                latitude = 19.0531, longitude = -104.3188,
                colonia = "San Pedrito", niveles = 1, jardinPatio = false, mascotasPermitidas = true
            ),
            Property(
                id = "8", title = "CASA RESIDENCIAL TAPEIXTLES", price = 1980000.0,
                listingType = ListingType.VENTA,
                location = "Col. Tapeixtles, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 140,
                bedrooms = 3, bathrooms = 2, garages = 2,
                images = listOf(R.drawable.prop_casa_fachada, R.drawable.prop_casa_cochera), isSaved = false,
                latitude = 19.1100, longitude = -104.3250,
                colonia = "Tapeixtles", niveles = 2, jardinPatio = true
            ),
            Property(
                id = "9", title = "DEPARTAMENTO VALLE DE LAS GARZAS", price = 9500.0,
                listingType = ListingType.RENTA,
                location = "Valle de las Garzas, Manzanillo, Colima",
                type = PropertyType.DEPARTAMENTO, areaSqM = 80,
                bedrooms = 2, bathrooms = 1, garages = 1,
                images = listOf(R.drawable.prop_depto_cocina, R.drawable.prop_depto_recamara), isSaved = false,
                latitude = 19.0900, longitude = -104.3150,
                colonia = "Valle de las Garzas", piso = 3, amueblado = false, elevador = true, mascotasPermitidas = false
            ),
            Property(
                id = "10", title = "CASA SOLEARES EN VENTA", price = 2100000.0,
                listingType = ListingType.VENTA,
                location = "Col. Soleares, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 150,
                bedrooms = 3, bathrooms = 2, garages = 2,
                images = listOf(R.drawable.prop_casa_jardin, R.drawable.prop_casa_sala2), isSaved = true,
                latitude = 19.1050, longitude = -104.3400,
                colonia = "Soleares", niveles = 2, jardinPatio = true
            ),
            Property(
                id = "11", title = "DEPARTAMENTO LA JOYA", price = 15000.0,
                listingType = ListingType.RENTA,
                location = "Col. La Joya, Manzanillo, Colima",
                type = PropertyType.DEPARTAMENTO, areaSqM = 90,
                bedrooms = 2, bathrooms = 2, garages = 1,
                images = listOf(R.drawable.prop_depto_sala, R.drawable.prop_depto_bano), isSaved = false,
                latitude = 19.1150, longitude = -104.3350,
                colonia = "La Joya", piso = 2, amueblado = true, elevador = true, mascotasPermitidas = true
            ),
            Property(
                id = "12", title = "TERRENO NUEVO SALAHUA", price = 550000.0,
                listingType = ListingType.VENTA,
                location = "Nuevo Salahua, Manzanillo, Colima",
                type = PropertyType.TERRENO, areaSqM = 250,
                frontMeters = 10.0, depthMeters = 25.0,
                images = listOf(R.drawable.prop_terreno_3, R.drawable.prop_terreno_1), isSaved = false,
                latitude = 19.1120, longitude = -104.3080,
                colonia = "Nuevo Salahua", usoSuelo = "Habitacional", superficieSqM = 250.0
            ),
            Property(
                id = "13", title = "TERRENO CAMPOS AGRÍCOLA", price = 800000.0,
                listingType = ListingType.VENTA,
                location = "Col. Campos, Manzanillo, Colima",
                type = PropertyType.TERRENO, areaSqM = 1200,
                frontMeters = 30.0, depthMeters = 40.0,
                images = listOf(R.drawable.prop_terreno_2, R.drawable.prop_terreno_5), isSaved = false,
                latitude = 19.1300, longitude = -104.3500,
                colonia = "Campos", usoSuelo = "Agrícola", superficieSqM = 1200.0
            ),
            Property(
                id = "14", title = "CASA EL COLOMO", price = 1250000.0,
                listingType = ListingType.VENTA,
                location = "El Colomo, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 130,
                bedrooms = 3, bathrooms = 1, garages = 1,
                images = listOf(R.drawable.prop_casa_fachada, R.drawable.prop_casa_cocina), isSaved = false,
                latitude = 19.1400, longitude = -104.3600,
                colonia = "El Colomo", niveles = 1, jardinPatio = true
            ),
            Property(
                id = "15", title = "DEPARTAMENTO LAS HADAS", price = 22000.0,
                listingType = ListingType.RENTA,
                location = "Las Hadas, Manzanillo, Colima",
                type = PropertyType.DEPARTAMENTO, areaSqM = 120,
                bedrooms = 3, bathrooms = 2, garages = 2,
                images = listOf(R.drawable.prop_depto_terraza, R.drawable.prop_depto_sala), isSaved = true,
                latitude = 19.0990, longitude = -104.3125,
                colonia = "Las Hadas", piso = 4, amueblado = true, elevador = true, mascotasPermitidas = true
            ),
            Property(
                id = "16", title = "CASA CLUB SANTIAGO", price = 3200000.0,
                listingType = ListingType.VENTA,
                location = "Club Santiago, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 220,
                bedrooms = 4, bathrooms = 3, garages = 2,
                images = listOf(R.drawable.prop_casa_jardin, R.drawable.prop_casa_fachada2), isSaved = false,
                latitude = 19.0960, longitude = -104.3060,
                colonia = "Club Santiago", niveles = 2, jardinPatio = true
            ),
            Property(
                id = "17", title = "TERRENO MIRAMAR", price = 700000.0,
                listingType = ListingType.VENTA,
                location = "Miramar, Manzanillo, Colima",
                type = PropertyType.TERRENO, areaSqM = 350,
                frontMeters = 12.0, depthMeters = 29.1,
                images = listOf(R.drawable.prop_terreno_4, R.drawable.prop_terreno_1), isSaved = false,
                latitude = 19.1220, longitude = -104.3460,
                colonia = "Miramar", usoSuelo = "Habitacional", superficieSqM = 350.0
            ),
            Property(
                id = "18", title = "DEPARTAMENTO SALAHUA", price = 8500.0,
                listingType = ListingType.RENTA,
                location = "Salahua, Manzanillo, Colima",
                type = PropertyType.DEPARTAMENTO, areaSqM = 65,
                bedrooms = 1, bathrooms = 1, garages = 1,
                images = listOf(R.drawable.prop_depto_recamara2, R.drawable.prop_depto_bano), isSaved = false,
                latitude = 19.1010, longitude = -104.3310,
                colonia = "Salahua", piso = 1, amueblado = false, elevador = false, mascotasPermitidas = false
            ),
            Property(
                id = "19", title = "CASA LAS BRISAS", price = 2800000.0,
                listingType = ListingType.VENTA,
                location = "Las Brisas, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 180,
                bedrooms = 3, bathrooms = 2, garages = 2,
                images = listOf(R.drawable.prop_casa_sala2, R.drawable.prop_casa_recamara), isSaved = false,
                latitude = 19.1005, longitude = -104.3305,
                colonia = "Las Brisas", niveles = 2, jardinPatio = true
            ),
            Property(
                id = "20", title = "CASA CENTRO HISTÓRICO", price = 1650000.0,
                listingType = ListingType.VENTA,
                location = "Centro Histórico, Manzanillo, Colima",
                type = PropertyType.CASA, areaSqM = 150,
                bedrooms = 3, bathrooms = 2, garages = 1,
                images = listOf(R.drawable.prop_casa_fachada, R.drawable.prop_casa_sala), isSaved = true,
                latitude = 19.0540, longitude = -104.3190,
                colonia = "Centro Histórico", niveles = 1, jardinPatio = true
            )
        )
    )

    override fun observeAll(): Flow<List<Property>> = properties

    override fun observeSaved(): Flow<List<Property>> =
        properties.map { list -> list.filter { it.isSaved } }

    override suspend fun getById(propertyId: String): Property? =
        properties.value.firstOrNull { it.id == propertyId }

    override suspend fun setSaved(propertyId: String, saved: Boolean) {
        properties.update { list ->
            list.map { if (it.id == propertyId) it.copy(isSaved = saved) else it }
        }
    }
}
