package com.equipo4.nearhome

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.util.NearHouseSounds

class NearHouseApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        AppStrings.init(this)
        NearHouseSounds.init(this)
    }

    // Libera el SoundPool cuando la app deja de verse (se vuelve a cargar solo si hace falta)
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) NearHouseSounds.release()
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(NetworkModule.provideOkHttpClient(SessionManager(this)))
            .build()
}