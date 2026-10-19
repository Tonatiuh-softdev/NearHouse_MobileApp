package com.equipo4.nearhome

import android.app.Application
import com.equipo4.nearhome.util.AppStrings

class NearHouseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStrings.init(this)
    }
}