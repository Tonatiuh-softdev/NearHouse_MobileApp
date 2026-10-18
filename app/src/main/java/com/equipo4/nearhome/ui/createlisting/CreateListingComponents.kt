package com.equipo4.nearhome.ui.createlisting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R
import java.text.NumberFormat
import java.util.Locale

/** Barra superior + "Paso X de 4" + porcentaje + barra de 4 segmentos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepHeader(title: String, step: Int, onBack: () -> Unit, totalSteps: Int = 4) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.background(cs.surface)) {
        CenterAlignedTopAppBar(
            title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = cs.surface)
        )
        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.create_listing_step_format, step, totalSteps),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.create_listing_percent_format, step * 100 / totalSteps),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(totalSteps) { i ->
                    Box(
                        Modifier.weight(1f).height(4.dp).clip(CircleShape)
                            .background(if (i < step) cs.primary else cs.outlineVariant)
                    )
                }
            }
        }
    }
}

/** Diálogo "¿Descartar anuncio?". */
@Composable
fun DiscardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_listing_discard_title)) },
        text = { Text(stringResource(R.string.create_listing_discard_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.create_listing_discard_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.create_listing_discard_keep)) }
        }
    )
}

/** Caja seleccionable (operación, tipo de inmueble, uso del suelo). */
@Composable
fun SelectableBox(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) cs.primaryContainer else cs.surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) cs.primary else cs.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun LabeledField(label: String, error: String?, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

/** Cabecera de un campo desplegable (el panel se muestra debajo, como en Filtros). */
@Composable
fun DropdownHeader(
    text: String?,
    placeholder: String,
    expanded: Boolean,
    isError: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val borderColor = when {
        isError -> cs.error
        text != null -> cs.primary
        else -> cs.outline
    }
    Row(
        Modifier.fillMaxWidth().height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(if (text != null) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text ?: placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = if (text != null) cs.onSurface else cs.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null, tint = cs.onSurfaceVariant
        )
    }
}

/** Opción numérica: [value] se guarda, [label] se muestra en la cabecera y [chip] en el botón. */
data class NumOption(val value: Int, val label: String, val chip: String = label)

/** Panel con botones numéricos (1, 2, 3...), como en la pantalla de Filtros. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberOptionsPanel(options: List<NumOption>, selected: Int?, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier.fillMaxWidth().clip(shape)
            .background(cs.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, cs.outlineVariant, shape)
            .padding(8.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { o ->
                val isSelected = o.value == selected
                Box(
                    Modifier.size(width = 46.dp, height = 38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) cs.primaryContainer else cs.surface)
                        .border(1.dp, if (isSelected) cs.primary else cs.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable { onSelect(o.value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        o.chip,
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                        color = if (isSelected) cs.primary else cs.onSurface, maxLines = 1
                    )
                }
            }
        }
    }
}

/** Panel de elección única en 2 columnas (colonia), con el mismo estilo que Ubicación en Filtros. */
@Composable
fun SingleChoicePanel(options: List<String>, selected: String?, onSelect: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape)
            .background(cs.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, cs.outlineVariant, shape)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowItems.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) cs.primaryContainer else cs.surface.copy(alpha = 0f))
                            .clickable { onSelect(option) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectIndicator(isSelected)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            option,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) cs.primary else cs.onSurface,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (rowItems.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SelectIndicator(selected: Boolean) {
    val cs = MaterialTheme.colorScheme
    if (selected) {
        Box(Modifier.size(20.dp).background(cs.primary, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, null, tint = cs.onPrimary, modifier = Modifier.size(12.dp))
        }
    } else {
        Box(
            Modifier.size(20.dp)
                .background(cs.surface, RoundedCornerShape(4.dp))
                .border(1.dp, cs.outline, RoundedCornerShape(4.dp))
        )
    }
}

/** Fila de checklist: etiqueta a la izquierda y checkbox a la derecha. */
@Composable
fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Checkbox(checked = checked, onCheckedChange = null)
    }
}

/** Muestra $18,500 mientras el valor guardado son solo dígitos. */
class MoneyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formatted = "\$" + NumberFormat.getIntegerInstance(Locale.US).format(digits.toLong())
        val len = digits.length
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val commas = (1 until offset).count { (len - it) % 3 == 0 }
                return 1 + offset + commas
            }

            override fun transformedToOriginal(offset: Int): Int {
                for (o in 0..len) if (originalToTransformed(o) >= offset) return o
                return len
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}