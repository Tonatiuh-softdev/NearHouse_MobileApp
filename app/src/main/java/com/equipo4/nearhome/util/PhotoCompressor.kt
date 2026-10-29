package com.equipo4.nearhome.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Las fotos de la cámara pesan varios MB y el API acepta máximo 2 MB por foto.
 * Aquí se reducen a 1600 px por el lado largo, se corrige la rotación (EXIF) y se guardan como JPEG.
 */
object PhotoCompressor {
    private const val MAX_SIDE = 1600
    private const val MAX_BYTES = 1_800_000   // margen por debajo de los 2 MB del API

    fun compress(path: String): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        check(bounds.outWidth > 0 && bounds.outHeight > 0) { "No se pudo leer la foto" }

        // Se decodifica ya reducida (potencias de 2) para no llenar la memoria con la foto completa
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: error("No se pudo leer la foto")

        val matrix = Matrix()
        aplicarOrientacion(matrix, path)
        val escala = min(1f, MAX_SIDE.toFloat() / max(decoded.width, decoded.height))
        if (escala < 1f) matrix.postScale(escala, escala)
        val final = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (final !== decoded) decoded.recycle()

        var calidad = 85
        var bytes: ByteArray
        do {
            val salida = ByteArrayOutputStream()
            final.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
            bytes = salida.toByteArray()
            calidad -= 10
        } while (bytes.size > MAX_BYTES && calidad >= 35)
        final.recycle()
        return bytes
    }

    private fun aplicarOrientacion(matrix: Matrix, path: String) {
        val orientacion = try {
            ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
        }
    }
}