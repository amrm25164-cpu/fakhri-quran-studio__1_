package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LayerProperties
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark

/**
 * Reusable Manual Font Size Stepper component:
 * - Decrease button (-)
 * - Increase button (+)
 * - Numeric text field for exact entry
 * - Continuous slider
 * - Reset button
 */
@Composable
fun ManualFontSizeStepper(
    label: String,
    currentSize: Float,
    minSize: Float,
    maxSize: Float,
    onSizeChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    var textInput by remember(currentSize) { mutableStateOf(currentSize.toInt().toString()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHighDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = IslamicCream,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onReset,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = IslamicGoldLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إعادة ضبط", color = IslamicGoldLight, fontSize = 10.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stepper Row: [-] [Numeric Input] [+] [Current Size Display]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Minus Button
                IconButton(
                    onClick = {
                        val newSize = (currentSize - 1f).coerceAtLeast(minSize)
                        onSizeChange(newSize)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerDark)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "تقليل الحجم", tint = IslamicCream)
                }

                // Numeric Input Field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { input ->
                        textInput = input
                        val parsed = input.toFloatOrNull()
                        if (parsed != null && parsed in minSize..maxSize) {
                            onSizeChange(parsed)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldPrimary
                    ),
                    modifier = Modifier.width(70.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = IslamicEmeraldLight,
                        focusedContainerColor = SurfaceContainerDark,
                        unfocusedContainerColor = SurfaceContainerDark
                    )
                )

                // Plus Button
                IconButton(
                    onClick = {
                        val newSize = (currentSize + 1f).coerceAtMost(maxSize)
                        onSizeChange(newSize)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerDark)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "زيادة الحجم", tint = IslamicCream)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${currentSize.toInt()} sp",
                    color = IslamicGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Slider
            Slider(
                value = currentSize,
                onValueChange = { onSizeChange(it) },
                valueRange = minSize..maxSize,
                colors = SliderDefaults.colors(
                    thumbColor = IslamicGoldPrimary,
                    activeTrackColor = IslamicGoldPrimary
                )
            )
        }
    }
}

/**
 * Reusable Layer Direct Manipulation & Alignment controls:
 * - Center X / Center Y
 * - Align Top / Middle / Bottom
 * - Sliders for X and Y ratios
 * - Lock toggle
 * - Reset position
 */
@Composable
fun LayerPositionControls(
    layer: LayerProperties,
    onUpdateLayer: (LayerProperties) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHighDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "موضع ومحاذاة الطبقة (${layer.name})",
                    color = IslamicCream,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Lock Toggle
                IconButton(
                    onClick = { onUpdateLayer(layer.copy(isLocked = !layer.isLocked)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "قفل الموضع",
                        tint = if (layer.isLocked) Color(0xFFE57373) else IslamicCream.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Alignment Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateLayer(layer.copy(offsetXRatio = 0.5f)) },
                    enabled = !layer.isLocked,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("توسيط أفقي", fontSize = 9.sp, color = IslamicCream)
                }
                OutlinedButton(
                    onClick = { onUpdateLayer(layer.copy(offsetYRatio = 0.15f)) },
                    enabled = !layer.isLocked,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("أعلى", fontSize = 9.sp, color = IslamicCream)
                }
                OutlinedButton(
                    onClick = { onUpdateLayer(layer.copy(offsetYRatio = 0.48f)) },
                    enabled = !layer.isLocked,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("وسط", fontSize = 9.sp, color = IslamicCream)
                }
                OutlinedButton(
                    onClick = { onUpdateLayer(layer.copy(offsetYRatio = 0.78f)) },
                    enabled = !layer.isLocked,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("أسفل", fontSize = 9.sp, color = IslamicCream)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vertical Offset Slider (Y)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "الموضع الرأسي (Y): ${(layer.offsetYRatio * 100).toInt()}%", color = IslamicCream, fontSize = 11.sp)
            }
            Slider(
                value = layer.offsetYRatio,
                onValueChange = { onUpdateLayer(layer.copy(offsetYRatio = it)) },
                enabled = !layer.isLocked,
                valueRange = 0.05f..0.95f,
                colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
            )

            // Horizontal Offset Slider (X)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "الموضع الأفقي (X): ${(layer.offsetXRatio * 100).toInt()}%", color = IslamicCream, fontSize = 11.sp)
            }
            Slider(
                value = layer.offsetXRatio,
                onValueChange = { onUpdateLayer(layer.copy(offsetXRatio = it)) },
                enabled = !layer.isLocked,
                valueRange = 0.10f..0.90f,
                colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
            )
        }
    }
}
