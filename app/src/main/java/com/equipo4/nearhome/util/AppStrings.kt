package com.equipo4.nearhome.util

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Acceso a strings.xml desde código que no es @Composable (ViewModels, repositorios, enums).
 * Respeta el idioma elegido en la app (AppCompatDelegate.setApplicationLocales).
 * Se inicializa en [com.equipo4.nearhome.NearHouseApplication].
 */
object AppStrings {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun locale(): Locale {
        val selected = AppCompatDelegate.getApplicationLocales()
        return if (!selected.isEmpty) selected[0] ?: Locale.getDefault() else Locale.getDefault()
    }

    fun get(@StringRes id: Int, vararg args: Any): String {
        val base = checkNotNull(appContext) { "AppStrings.init() no fue llamado" }
        val config = Configuration(base.resources.configuration).apply { setLocale(locale()) }
        val localized = base.createConfigurationContext(config)
        return if (args.isEmpty()) localized.getString(id) else localized.getString(id, *args)
    }
}