package com.equipo4.nearhome.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object PhotoProcessor {
    private const val MAX_LADO = 1024

    /** Reduce la imagen a un máximo de 1024 px por lado y la regresa como JPEG. */
    fun comprimirAJpeg(context: Context, uri: Uri): ByteArray? {
        val original = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: return null

        val escala = minOf(1f, MAX_LADO.toFloat() / maxOf(original.width, original.height))
        val bitmap = if (escala < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * escala).toInt().coerceAtLeast(1),
                (original.height * escala).toInt().coerceAtLeast(1),
                true
            )
        } else {
            original
        }

        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }
    }
}