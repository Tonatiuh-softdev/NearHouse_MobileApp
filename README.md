# 5D_EQ4
App movil de servicio de gestion inmoviliario

## Mapa con Google Maps: configuración de la API key

La pantalla **Mapa** usa Google Maps (Maps SDK for Android + Maps Compose). Para que el mapa se vea y no salga gris, **cada integrante** debe hacer lo siguiente en su computadora.

> ⚠️ **La API key nunca se sube a GitHub.** Vive en `secrets.properties`, que está en el `.gitignore`. Se comparte con el equipo por mensaje privado.

### 1. Crear tu `secrets.properties`

En la **raíz del proyecto** (junto a `settings.gradle.kts`) crea un archivo llamado `secrets.properties` con esta única línea, sin espacios ni comillas:

```
MAPS_API_KEY=PEGA_AQUI_LA_KEY
```

Reemplaza `PEGA_AQUI_LA_KEY` por la key que te compartió la persona que administra el proyecto de Google Cloud.

Si no tienes la key, el proyecto **sí compila**: usa el valor de ejemplo de `local.defaults.properties` (`MAPS_API_KEY=DEFAULT_API_KEY`), pero el mapa saldrá gris.

### 2. Obtener tu huella SHA-1

Cada computadora tiene su propia huella. En la terminal de Android Studio, en la raíz del proyecto:

```
.\gradlew signingReport
```

En Windows PowerShell se escribe `.\gradlew`; en macOS/Linux, `./gradlew`. Si la terminal dice que no encuentra Java, usa el panel **Gradle** de Android Studio: `app` → `Tasks` → `android` → `signingReport`.

En la salida, busca la variante **debug** y copia la línea que empieza con `SHA1:`.

### 3. Registrar tu SHA-1 en la key

Envía tu SHA-1 a la persona que administra la key. Esa persona la agrega en **Google Cloud Console → APIs y servicios → Credenciales → (la key) → Restricciones de aplicaciones → Apps de Android**, con:

- **Nombre del paquete:** `com.equipo4.nearhome`
- **Huella SHA-1:** la de cada integrante (un elemento por persona)

Los cambios pueden tardar unos minutos en aplicarse.

### 4. Sincronizar y correr

1. En Android Studio: **File → Sync Project with Gradle Files**.
2. Corre la app en un celular o en un emulador con imagen **Google APIs** o **Google Play**.
3. Abre la pestaña **Mapa**. Debe verse Manzanillo con los pines de las propiedades.

### Restricciones de la key (para quien la administra)

- **Restricción de API:** solo *Maps SDK for Android*.
- **Restricción de aplicación:** solo apps Android con el paquete `com.equipo4.nearhome` y las SHA-1 de los integrantes.
- Google pide una cuenta de facturación en el proyecto; el uso del mapa en apps móviles es gratuito e ilimitado según su documentación.

### Problemas frecuentes

| Síntoma | Causa probable |
|---|---|
| El mapa sale **gris** | Falta tu SHA-1 en la key, el paquete no coincide exactamente con `com.equipo4.nearhome`, o la key en `secrets.properties` está mal escrita |
| Sigue gris después de agregar la SHA-1 | Esperar unos minutos y volver a abrir la app |
| El mapa no aparece en el emulador | La imagen del emulador no trae servicios de Google; usar una **Google APIs** o **Google Play** |
| Error al compilar de `MAPS_API_KEY` | Falta `local.defaults.properties` o el `<meta-data>` del manifiesto |
| Errores "incompatible version of Kotlin" | Hacer **Sync** de Gradle: el proyecto usa Kotlin 2.4.10 desde esta rama |

### Cómo funciona (resumen técnico)

- El **Secrets Gradle Plugin** lee `secrets.properties` (o, si no existe, `local.defaults.properties`) y expone `MAPS_API_KEY` al manifiesto.
- En `AndroidManifest.xml` se lee con el metadato `com.google.android.geo.API_KEY`.
- El mapa se dibuja con `GoogleMap` (Maps Compose), con un marcador por propiedad visible. Los marcadores cercanos se agrupan (clustering) y al tocar uno se abre la hoja de vista previa.