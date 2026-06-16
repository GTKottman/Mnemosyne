package com.gtnoo.mnemosyne.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gtnoo.mnemosyne.domain.model.*

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
    onValueChange: (Int) -> Unit
) {
    Column(modifier = modifier) {
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

@Composable
fun WeatherCard(snapshot: WeatherSnapshot, modifier: Modifier = Modifier) {
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
                    Text("${it.toInt()}°C avg", style = MaterialTheme.typography.bodySmall)
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.entryDate.toString(), style = MaterialTheme.typography.titleSmall)
                Text(entry.context.entryType.label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            if (entry.interactions.isNotEmpty()) {
                Text(
                    entry.interactions.joinToString(", ") { it.person.displayName },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmotionPill("Anx", entry.emotionData.anxious)
                EmotionPill("Happy", entry.emotionData.happy)
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
