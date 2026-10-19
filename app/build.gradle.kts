plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.equipo4.nearhome"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.equipo4.nearhome"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // ViewModel para Jetpack Compose (resuelve el error de viewModel)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Iconos extendidos de Material Design (resuelve el error de Icons)
    implementation("androidx.compose.material:material-icons-extended")

    // Agrega esta línea para habilitar la navegación:
    implementation("androidx.navigation:navigation-compose:2.8.1")

    // Pantalla de carga: animación Lottie + Splash Screen API nativa
    implementation(libs.lottie.compose)
    implementation(libs.androidx.core.splashscreen)
}