package com.gtnoo.mnemosyne.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

private const val DEFAULT_ZOOM = 12.0
private val DEFAULT_CENTER = GeoPoint(39.8283, -98.5795) // center of contiguous US

@Composable
fun LocationPickerDialog(
    initialLat: Double?,
    initialLng: Double?,
    cityHint: String,
    onConfirm: (lat: Double, lng: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(false)
                        isTilesScaledToDpi = true

                        val start = when {
                            initialLat != null && initialLng != null ->
                                GeoPoint(initialLat, initialLng)
                            else -> DEFAULT_CENTER
                        }
                        controller.setZoom(DEFAULT_ZOOM)
                        controller.setCenter(start)

                        overlays.add(object : Overlay() {
                            override fun onTouchEvent(e: MotionEvent, mapView: MapView): Boolean {
                                isDragging = e.action == MotionEvent.ACTION_MOVE
                                return false
                            }
                        })

                        mapViewRef.value = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Fixed crosshair pin at map center
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = "Selected location",
                        tint = if (isDragging)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }

            // Top bar with title and close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = if (cityHint.isNotBlank()) "Pin your location in $cityHint" else "Pan and pin your location",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }

            // Confirm button at the bottom
            Button(
                onClick = {
                    val center = mapViewRef.value?.mapCenter
                    if (center != null) {
                        onConfirm(center.latitude, center.longitude)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(50)),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Confirm Location")
            }
        }
    }
}
