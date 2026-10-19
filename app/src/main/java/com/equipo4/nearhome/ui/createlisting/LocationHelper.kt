package com.equipo4.nearhome.ui.createlisting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * [hints] son los posibles nombres de colonia que devolvió el geocoder (subLocality y cada
 * segmento de las líneas de dirección), en orden de prioridad. El ViewModel los compara con el
 * catálogo de colonias de la app.
 */
data class GeoAddress(
    val street: String,
    val number: String,
    val postalCode: String,
    val colonia: String,
    val hints: List<String> = emptyList()
)

object LocationHelper {

    private const val TAG = "NearHomeGeo"

    /** Ubicación GPS actual del celular. Null si no hay permiso, no hay GPS o se agota el tiempo. */
    @SuppressLint("MissingPermission")
    suspend fun current(context: Context): Location? {
        val granted = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (!granted) return null

        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        return withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                cont.invokeOnCancellation { cts.cancel() }
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }
    }

    /**
     * Calle, número, C.P. y posibles colonias que da Geocoder. Null si falla; entonces se escriben a mano.
     * Se piden varios resultados porque el primero muchas veces no trae la colonia.
     */
    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): GeoAddress? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            try {
                val geocoder = Geocoder(context, Locale.forLanguageTag("es-MX"))
                val addresses: List<Address> = if (Build.VERSION.SDK_INT >= 33) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 5, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                cont.resume(addresses.toList())
                            }

                            override fun onError(errorMessage: String?) {
                                cont.resume(emptyList())
                            }
                        })
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 5).orEmpty()
                }
                if (addresses.isEmpty()) return@withContext null

                // Calle y número del mismo resultado: el primero que tenga calle
                val best = addresses.firstOrNull { !it.thoroughfare.isNullOrBlank() } ?: addresses.first()
                val postal = best.postalCode?.takeIf { it.isNotBlank() }
                    ?: addresses.firstNotNullOfOrNull { it.postalCode?.takeIf { cp -> cp.isNotBlank() } }
                    ?: ""

                // Posibles colonias: subLocality primero y luego cada segmento de las líneas de dirección
                val hints = buildList {
                    addresses.forEach { a -> a.subLocality?.let { add(it) } }
                    addresses.forEach { a ->
                        for (i in 0..a.maxAddressLineIndex) {
                            a.getAddressLine(i)?.split(",")?.forEach { add(it.trim()) }
                        }
                    }
                }.filter { it.isNotBlank() }.distinct()

                // Para depurar: en Logcat filtra por NearHomeGeo
                Log.d(TAG, "($lat, $lng) -> hints=$hints")

                GeoAddress(
                    street = best.thoroughfare.orEmpty(),
                    number = best.subThoroughfare.orEmpty(),
                    postalCode = postal,
                    colonia = addresses.firstNotNullOfOrNull { it.subLocality?.takeIf { s -> s.isNotBlank() } }.orEmpty(),
                    hints = hints
                )
            } catch (e: Exception) {
                Log.d(TAG, "Geocoder falló: ${e.message}")
                null
            }
        }
}