package com.equipo4.nearhome

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.util.AppStrings

class NearHouseApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        AppStrings.init(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(NetworkModule.provideOkHttpClient(SessionManager(this)))
            .build()
}