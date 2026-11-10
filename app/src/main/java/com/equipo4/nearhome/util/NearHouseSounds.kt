package com.equipo4.nearhome.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.equipo4.nearhome.R

/**
 * Sonidos de interfaz de NearHouse (carga y logro) con SoundPool: suenan sin retraso.
 * - Se cargan una sola vez (init) y se liberan cuando la app pasa a segundo plano (release).
 * - Respeta el modo del celular: en silencio o vibrar no suena.
 * El sonido de notificación NO pasa por aquí: lo reproduce el canal de notificaciones.
 */
object NearHouseSounds {

    enum class Sound(val rawRes: Int) {
        CARGA(R.raw.sonido_carga),
        LOGRO(R.raw.sonido_logro)
    }

    private var pool: SoundPool? = null
    private val ids = mutableMapOf<Sound, Int>()
    private val loaded = mutableSetOf<Int>()
    private val pending = mutableSetOf<Int>()

    /** Carga los sonidos una sola vez. Seguro de llamar varias veces. */
    @Synchronized
    fun init(context: Context) {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()
        sp.setOnLoadCompleteListener { p, sampleId, status ->
            synchronized(this) {
                if (status == 0) {
                    loaded.add(sampleId)
                    // Si alguien pidió reproducirlo mientras cargaba, suena en cuanto esté listo
                    if (pending.remove(sampleId)) p.play(sampleId, 1f, 1f, 1, 0, 1f)
                }
            }
        }
        val app = context.applicationContext
        Sound.values().forEach { ids[it] = sp.load(app, it.rawRes, 1) }
        pool = sp
    }

    /** Reproduce el sonido, salvo que el celular esté en silencio o vibrar. */
    @Synchronized
    fun play(context: Context, sound: Sound) {
        val audio = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        init(context)
        val id = ids[sound] ?: return
        if (loaded.contains(id)) pool?.play(id, 1f, 1f, 1, 0, 1f) else pending.add(id)
    }

    /** Libera el SoundPool. Si se vuelve a necesitar, play() lo carga de nuevo. */
    @Synchronized
    fun release() {
        pool?.release()
        pool = null
        ids.clear()
        loaded.clear()
        pending.clear()
    }
}

/**
 * Ponlo dentro de cualquier pantalla/componente con la palomita verde de éxito:
 * suena una sola vez al aparecer (no se repite al redibujarse ni al recrearse la pantalla).
 */
@Composable
fun LogroSonidoEffect() {
    val context = LocalContext.current
    var yaSono by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!yaSono) {
            yaSono = true
            NearHouseSounds.play(context, NearHouseSounds.Sound.LOGRO)
        }
    }
}
