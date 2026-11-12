package com.gaming.companion.aimtraining

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaming.companion.ui.components.GamingTopBar
import com.gaming.companion.ui.components.GlassCard
import com.gaming.companion.ui.components.NeonButton
import com.gaming.companion.ui.components.StatBadge
import com.gaming.companion.ui.theme.BgDark
import com.gaming.companion.ui.theme.CardDark
import com.gaming.companion.ui.theme.NeonCyan
import com.gaming.companion.ui.theme.NeonGreen
import com.gaming.companion.ui.theme.NeonPurple
import com.gaming.companion.ui.theme.NeonRed
import com.gaming.companion.ui.theme.NeonYellow
import com.gaming.companion.ui.theme.TextMuted
import com.gaming.companion.ui.theme.TextPrimary
import com.gaming.companion.ui.theme.TextSecondary

@Composable
fun AimTrainingScreen(
    viewModel: AimTrainingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        GamingTopBar(
            title = "AIM TRAINER",
            subtitle = "NECK & HEAD DRILL SIMULATOR"
        )

        // Top Statistics HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBadge(
                label = "Accuracy",
                value = "${String.format("%.1f", state.accuracy)}%",
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = "Hits / Shots",
                value = "${state.hits}/${state.totalShots}",
                accentColor = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = "Headshots",
                value = "${state.headshots}",
                accentColor = NeonYellow,
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = "Timer",
                value = "${state.remainingTimeSeconds}s",
                accentColor = if (state.remainingTimeSeconds < 10) NeonRed else NeonPurple,
                modifier = Modifier.weight(1f)
            )
        }

        // Training Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardDark.copy(alpha = 0.6f))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        viewModel.processShot(offset.x, offset.y)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragStartOffset = offset
                            viewModel.updateCrosshair(offset.x, offset.y)
                        },
                        onDragEnd = {
                            dragStartOffset = null
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newX = state.crosshairX + dragAmount.x
                            val newY = state.crosshairY + dragAmount.y
                            viewModel.updateCrosshair(newX, newY)
                            // Fire shot on upward drag flick
                            if (dragAmount.y < -15f && dragStartOffset != null) {
                                viewModel.processShot(newX, newY, dragStartOffset?.x, dragStartOffset?.y)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val target = state.target

                // Grid background lines for aim alignment
                val step = 80f
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(
                        color = Color(0x1100E5FF),
                        start = Offset(x * step, 0f),
                        end = Offset(x * step, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(
                        color = Color(0x1100E5FF),
                        start = Offset(0f, y * step),
                        end = Offset(size.width, y * step),
                        strokeWidth = 1f
                    )
                }

                // 1. Draw Target Silhouette & Hit Zones
                val targetCenter = Offset(target.x, target.y)

                // Outer Body Zone (Purple)
                drawCircle(
                    color = NeonPurple.copy(alpha = 0.35f),
                    radius = target.radius,
                    center = targetCenter
                )
                drawCircle(
                    color = NeonPurple,
                    radius = target.radius,
                    center = targetCenter,
                    style = Stroke(width = 3f)
                )

                // Neck Practice Zone (Cyan Band)
                val neckRadius = target.radius * 0.6f
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.45f),
                    radius = neckRadius,
                    center = targetCenter.copy(y = targetCenter.y - (target.radius * 0.15f))
                )
                drawCircle(
                    color = NeonCyan,
                    radius = neckRadius,
                    center = targetCenter.copy(y = targetCenter.y - (target.radius * 0.15f)),
                    style = Stroke(width = 2.5f)
                )

                // Headshot Critical Zone (Gold/Yellow Bullseye)
                val headRadius = target.radius * 0.35f
                val headCenter = targetCenter.copy(y = targetCenter.y - (target.radius * 0.40f))
                drawCircle(
                    color = NeonYellow.copy(alpha = 0.85f),
                    radius = headRadius,
                    center = headCenter
                )
                drawCircle(
                    color = NeonRed,
                    radius = headRadius,
                    center = headCenter,
                    style = Stroke(width = 3f)
                )

                // 2. Draggable Neon Crosshair
                val crosshairOffset = Offset(state.crosshairX, state.crosshairY)
                val crossSize = 24f

                // Outer Reticle Ring
                drawCircle(
                    color = NeonCyan,
                    radius = 16f,
                    center = crosshairOffset,
                    style = Stroke(width = 2f)
                )
                // Center Dot
                drawCircle(
                    color = NeonRed,
                    radius = 3.5f,
                    center = crosshairOffset
                )
                // Cross lines
                drawLine(
                    color = NeonCyan,
                    start = Offset(crosshairOffset.x - crossSize, crosshairOffset.y),
                    end = Offset(crosshairOffset.x - 6f, crosshairOffset.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = NeonCyan,
                    start = Offset(crosshairOffset.x + 6f, crosshairOffset.y),
                    end = Offset(crosshairOffset.x + crossSize, crosshairOffset.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = NeonCyan,
                    start = Offset(crosshairOffset.x, crosshairOffset.y - crossSize),
                    end = Offset(crosshairOffset.x, crosshairOffset.y - 6f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = NeonCyan,
                    start = Offset(crosshairOffset.x, crosshairOffset.y + 6f),
                    end = Offset(crosshairOffset.x, crosshairOffset.y + crossSize),
                    strokeWidth = 2f
                )
            }

            // Overlay Feedback Toast for recent hits
            state.recentFeedbacks.firstOrNull()?.let { fb ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (fb.zone) {
                                3 -> NeonYellow.copy(alpha = 0.25f)
                                2 -> NeonCyan.copy(alpha = 0.25f)
                                1 -> NeonPurple.copy(alpha = 0.25f)
                                else -> NeonRed.copy(alpha = 0.25f)
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = fb.text,
                        color = when (fb.zone) {
                            3 -> NeonYellow
                            2 -> NeonCyan
                            1 -> NeonPurple
                            else -> NeonRed
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Bottom Controls: Sensitivity Slider & Session Buttons
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DRAG SENSITIVITY",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${String.format("%.2f", state.sensitivity)}x Scale",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = state.sensitivity,
                    onValueChange = { viewModel.setSensitivity(it) },
                    valueRange = 0.5f..3.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color(0xFF22304A)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!state.isSessionActive) {
                        NeonButton(
                            text = "START 30s DRILL",
                            onClick = { viewModel.startSession(30) },
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        NeonButton(
                            text = "STOP DRILL",
                            onClick = { viewModel.stopSession() },
                            icon = Icons.Default.Stop,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}
