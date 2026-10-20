package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.util.AppStrings

private const val PW = 400f
private const val PH = 200f

/**
 * Mapa ficticio pequeño para el detalle: calles, mar, parque, puntos de interés
 * (hospital, escuela, restaurante, tienda) y el pin de la propiedad al centro.
 */
@Composable
fun FakeMapPreview(propertyType: PropertyType, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current.density
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val pin = when (propertyType) {
        PropertyType.CASA -> R.drawable.ic_pin_casa
        PropertyType.TERRENO -> R.drawable.ic_pin_terreno
        PropertyType.DEPARTAMENTO -> R.drawable.ic_pin_departamento
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val s = maxOf(size.width / (PW * density), size.height / (PH * density))
            val dx = (size.width - PW * density * s) / 2f
            val dy = (size.height - PH * density * s) / 2f
            drawRect(if (isDark) Color(0xFF1B222A) else Color(0xFFEDE9E1))
            withTransform({
                translate(dx, dy)
                scale(s * density, s * density, pivot = Offset.Zero)
            }) { drawPreviewCity(textMeasurer, isDark) }
        }
        Image(
            painter = painterResource(pin),
            contentDescription = stringResource(R.string.property_location_cd),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-25).dp)
                .size(36.dp, 50.dp)
        )
    }
}

private fun DrawScope.drawPreviewCity(tm: TextMeasurer, isDark: Boolean) {
    val block = if (isDark) Color(0xFF242D38) else Color(0xFFE4DFD5)
    val park = if (isDark) Color(0xFF1E382B) else Color(0xFFC8E6B0)
    val water = if (isDark) Color(0xFF0F2233) else Color(0xFFA9D3F0)
    val roadBorder = if (isDark) Color(0xFF1E2834) else Color(0xFFD5D0C6)
    val roadFill = if (isDark) Color(0xFF2A3645) else Color.White
    val labelColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF4B5563)

    for (gx in 0 until 8) for (gy in 0 until 4) {
        drawRoundRect(block, Offset(gx * 50f + 7f, gy * 50f + 7f), Size(36f, 36f), CornerRadius(3f))
    }
    drawRoundRect(park, Offset(150f, 120f), Size(80f, 60f), CornerRadius(8f))
    drawOval(water, Offset(300f, 10f), Size(110f, 45f))
    val sea = Path().apply {
        moveTo(0f, 120f); cubicTo(40f, 130f, 70f, 160f, 80f, 200f); lineTo(0f, 200f); close()
    }
    drawPath(sea, water)

    fun road(width: Float, vararg xy: Float) {
        val p = Path().apply {
            moveTo(xy[0], xy[1]); var i = 2
            while (i < xy.size) { lineTo(xy[i], xy[i + 1]); i += 2 }
        }
        drawPath(p, roadBorder, style = Stroke(width + 3f, cap = StrokeCap.Round))
        drawPath(p, roadFill, style = Stroke(width, cap = StrokeCap.Round))
    }
    road(10f, 0f, 100f, 400f, 100f)
    road(10f, 200f, 0f, 200f, 200f)
    road(7f, 0f, 50f, 400f, 50f)
    road(7f, 100f, 0f, 100f, 120f)
    road(7f, 300f, 0f, 300f, 200f)
    road(7f, 0f, 150f, 400f, 150f)

    fun poi(x: Float, y: Float, color: Color, letter: String, label: String) {
        drawCircle(if (isDark) Color(0xFF1E2630) else Color.White, 12f, Offset(x, y))
        drawCircle(color, 10f, Offset(x, y))
        val l = tm.measure(letter, TextStyle(fontSize = 5.sp, color = Color.White, fontWeight = FontWeight.Bold))
        drawText(l, topLeft = Offset(x - l.size.width / 2f, y - l.size.height / 2f))
        val t = tm.measure(label, TextStyle(fontSize = 4.5.sp, color = labelColor, fontWeight = FontWeight.Medium))
        drawText(t, topLeft = Offset(x - t.size.width / 2f, y + 13f))
    }
    poi(75f, 28f, Color(0xFFD93025), "H", AppStrings.get(R.string.poi_hospital))
    poi(335f, 78f, Color(0xFF1E88E5), "E", AppStrings.get(R.string.poi_school))
    poi(285f, 168f, Color(0xFFF57C00), "R", AppStrings.get(R.string.poi_restaurant))
    poi(115f, 168f, Color(0xFF7B1FA2), "T", AppStrings.get(R.string.poi_store))
    val pk = tm.measure(AppStrings.get(R.string.poi_park), TextStyle(fontSize = 4.5.sp, color = if (isDark) Color(0xFF81C784) else Color(0xFF5B8C3A), fontWeight = FontWeight.Medium))
    drawText(pk, topLeft = Offset(190f - pk.size.width / 2f, 160f))
}
