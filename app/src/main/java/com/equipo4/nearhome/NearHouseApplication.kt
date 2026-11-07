package com.equipo4.nearhome

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.equipo4.nearhome.data.local.SettingsManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.util.AppStrings

class NearHouseApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        AppStrings.init(this)
        // El splash nativo (drawable-night) debe seguir el modo elegido en la app, no solo el del sistema
        AppCompatDelegate.setDefaultNightMode(
            when (SettingsManager(this).getDarkMode()) {
                true -> AppCompatDelegate.MODE_NIGHT_YES
                false -> AppCompatDelegate.MODE_NIGHT_NO
                null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(NetworkModule.provideOkHttpClient(SessionManager(this)))
            .build()
}