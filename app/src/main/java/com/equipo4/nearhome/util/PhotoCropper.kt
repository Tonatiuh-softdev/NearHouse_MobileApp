package com.equipo4.nearhome.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import java.io.File

/**
 * Carga y recorta la foto de perfil.
 * Se usa desde la pantalla de recorte circular (ProfilePhotoCropScreen).
 */
object PhotoCropper {
    private const val MAX_LADO_CARGA = 2048
    private const val LADO_SALIDA = 1024

    /**
     * Lee la imagen reduciéndola a un máximo de 2048 px por lado y la endereza
     * según su EXIF (las fotos de cámara suelen venir giradas).
     */
    fun cargar(context: Context, uri: Uri): Bitmap? = try {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            null
        } else {
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_LADO_CARGA) {
                sample *= 2
            }
            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            }
            bitmap?.let { enderezar(context, uri, it) }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun enderezar(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientacion = try {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val matrix = Matrix()
        when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Recorta el cuadrado [area] (en píxeles del bitmap) que encierra al círculo,
     * lo escala a máximo 1024 px y lo guarda como JPEG en almacenamiento interno.
     * La foto se guarda cuadrada: la app ya la muestra recortada en círculo.
     */
    fun recortarYGuardar(context: Context, bitmap: Bitmap, area: RectF): Uri? = try {
        val src = Rect(
            area.left.toInt().coerceIn(0, bitmap.width - 1),
            area.top.toInt().coerceIn(0, bitmap.height - 1),
            area.right.toInt().coerceIn(1, bitmap.width),
            area.bottom.toInt().coerceIn(1, bitmap.height)
        )
        val lado = minOf(LADO_SALIDA, maxOf(src.width(), src.height()))
        val salida = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
        Canvas(salida).drawBitmap(
            bitmap,
            src,
            Rect(0, 0, lado, lado),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )

        val archivo = File(context.filesDir, "profile_photo_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { salida.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        salida.recycle()
        Uri.fromFile(archivo)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
