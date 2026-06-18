package com.gtnoo.mnemosyne.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gtnoo.mnemosyne.domain.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CollapsibleSection(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun SliderField(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    range: IntRange = 0..10,
    leftLabel: String? = null,
    rightLabel: String? = null,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = modifier) {
        if (leftLabel != null && rightLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    leftLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    rightLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SwitchField(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

fun formatTemperature(celsius: Double, useFahrenheit: Boolean): String {
    val (value, unit) = if (useFahrenheit) {
        celsius * 9.0 / 5.0 + 32 to "°F"
    } else {
        celsius to "°C"
    }
    return "${value.toInt()}$unit avg"
}

@Composable
fun WeatherCard(
    snapshot: WeatherSnapshot,
    modifier: Modifier = Modifier,
    useFahrenheit: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(snapshot.placeLabel, style = MaterialTheme.typography.labelSmall)
                Text(
                    snapshot.condition.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                snapshot.temperatureAverage?.let {
                    Text(formatTemperature(it, useFahrenheit), style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                snapshot.humidity?.let { Text("Humidity: ${it.toInt()}%", style = MaterialTheme.typography.bodySmall) }
                snapshot.precipitationMm?.let { Text("Precip: ${it}mm", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
fun PersonChip(
    person: Person,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = true,
        onClick = {},
        label = { Text(person.displayName) },
        modifier = modifier,
        trailingIcon = onRemove?.let {
            { IconButton(onClick = it, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
            }}
        }
    )
}

@Composable
fun PlaceChip(
    place: SavedPlace,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = true,
        onClick = {},
        label = { Text(place.label) },
        modifier = modifier,
        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp)) },
        trailingIcon = onRemove?.let {
            { IconButton(onClick = it, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
            }}
        }
    )
}

@Composable
fun EntryCard(
    entry: com.gtnoo.mnemosyne.domain.model.DailyEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(entry.entryDate.toString(), style = MaterialTheme.typography.titleSmall)
            if (entry.interactions.isNotEmpty()) {
                Text(
                    entry.interactions.joinToString(", ") { it.person.displayName },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmotionPill("Mood", entry.emotionData.happiness)
                EmotionPill("Calm", entry.emotionData.calm)
                EmotionPill("Energy", entry.healthContextData.energyLevel)
            }
        }
    }
}

@Composable
private fun EmotionPill(label: String, value: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value.toString(), style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PersonCard(
    person: Person,
    summary: PersonSummary? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(person.displayName, style = MaterialTheme.typography.titleMedium)
                    if (person.isFavorite) {
                        Icon(Icons.Default.Star, contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(person.relationshipType.label, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                summary?.let {
                    Text("${it.totalInteractions} interactions · ${it.daysSinceLastContact ?: "?"} days ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text, modifier = modifier.padding(vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun NumberInputField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { text -> onValueChange(text.toIntOrNull() ?: 0) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
    )
}

/**
 * Read-only display row that mirrors the visual language of SliderField.
 * Pass [leftLabel] and [rightLabel] for bipolar axes; pass [label] alone for single-label fields.
 */
@Composable
fun StatValueRow(
    value: Int,
    modifier: Modifier = Modifier,
    label: String = "",
    leftLabel: String? = null,
    rightLabel: String? = null
) {
    if (leftLabel != null && rightLabel != null) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                leftLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                rightLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Generic radar / web chart drawn entirely with Compose Canvas.
 * [data] is a list of (label, value) pairs; values are expected on a 0–[maxValue] scale.
 * Requires at least 3 spokes.
 */
@Composable
fun RadarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    maxValue: Int = 10
) {
    val n = data.size
    if (n < 3) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = onSurface)
    // Pre-measure labels outside the draw lambda so layout is consistent each frame
    val measuredLabels = remember(data, labelStyle) {
        data.map { (label, _) -> textMeasurer.measure(label, labelStyle) }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val labelMargin = 44.dp.toPx()
        val chartRadius = (size.minDimension / 2f) - labelMargin

        val angles = List(n) { i -> -PI / 2.0 + i * 2.0 * PI / n }

        // Concentric grid rings at 2, 4, 6, 8, 10
        for (ring in 1..5) {
            val fraction = (ring * 2f) / maxValue.toFloat()
            val ringPath = Path()
            angles.forEachIndexed { i, angle ->
                val px = cx + (chartRadius * fraction * cos(angle)).toFloat()
                val py = cy + (chartRadius * fraction * sin(angle)).toFloat()
                if (i == 0) ringPath.moveTo(px, py) else ringPath.lineTo(px, py)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = gridColor.copy(alpha = if (ring == 5) 0.45f else 0.2f),
                style = Stroke(width = if (ring == 5) 1.5f else 0.8f)
            )
        }

        // Spoke lines from center to outermost ring
        angles.forEach { angle ->
            drawLine(
                color = gridColor.copy(alpha = 0.3f),
                start = Offset(cx, cy),
                end = Offset(
                    (cx + chartRadius * cos(angle)).toFloat(),
                    (cy + chartRadius * sin(angle)).toFloat()
                ),
                strokeWidth = 0.8f
            )
        }

        // Filled data polygon
        val dataPath = Path()
        data.forEachIndexed { i, (_, value) ->
            val fraction = value.coerceIn(0, maxValue).toFloat() / maxValue.toFloat()
            val px = cx + (chartRadius * fraction * cos(angles[i])).toFloat()
            val py = cy + (chartRadius * fraction * sin(angles[i])).toFloat()
            if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
        }
        dataPath.close()
        drawPath(dataPath, color = primaryColor.copy(alpha = 0.22f))
        drawPath(dataPath, color = primaryColor.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))

        // Dot at each data point
        data.forEachIndexed { i, (_, value) ->
            val fraction = value.coerceIn(0, maxValue).toFloat() / maxValue.toFloat()
            val px = cx + (chartRadius * fraction * cos(angles[i])).toFloat()
            val py = cy + (chartRadius * fraction * sin(angles[i])).toFloat()
            drawCircle(color = primaryColor, radius = 3.5f, center = Offset(px, py))
        }

        // Labels just outside the outermost ring
        val labelRadius = chartRadius + 28.dp.toPx()
        measuredLabels.forEachIndexed { i, layout ->
            val lx = (cx + labelRadius * cos(angles[i])).toFloat()
            val ly = (cy + labelRadius * sin(angles[i])).toFloat()
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = lx - layout.size.width / 2f,
                    y = ly - layout.size.height / 2f
                )
            )
        }
    }
}
