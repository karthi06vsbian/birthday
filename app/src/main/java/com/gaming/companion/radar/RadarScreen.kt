package com.gaming.companion.radar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaming.companion.ui.components.GamingTopBar
import com.gaming.companion.ui.components.GlassCard
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
fun RadarScreen(
    viewModel: RadarViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        GamingTopBar(
            title = "TACTICAL RADAR",
            subtitle = "100-METER SITUATIONAL SIMULATOR"
        )

        // Data Source & Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CardDark)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.dataSourceStatus,
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "LST: ${state.lastUpdatedTime}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Circular Tactical Radar Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(270.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f

                // Radar circular dark background
                drawCircle(
                    color = Color(0xFF0A0F1A),
                    radius = maxRadius,
                    center = center
                )

                // 25m, 50m, 75m, 100m Distance Rings
                val ringFractions = listOf(0.25f, 0.50f, 0.75f, 1.0f)
                ringFractions.forEach { frac ->
                    val r = maxRadius * frac
                    drawCircle(
                        color = if (frac == 1.0f) NeonCyan else Color(0x3300E5FF),
                        radius = r,
                        center = center,
                        style = Stroke(width = if (frac == 1.0f) 2f else 1f)
                    )
                }

                // Crosshairs (North, South, East, West)
                drawLine(
                    color = Color(0x3300E5FF),
                    start = Offset(center.x, center.y - maxRadius),
                    end = Offset(center.x, center.y + maxRadius),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0x3300E5FF),
                    start = Offset(center.x - maxRadius, center.y),
                    end = Offset(center.x + maxRadius, center.y),
                    strokeWidth = 1f
                )

                // Rotating Radar Sweep Line
                val sweepRad = Math.toRadians((state.sweepAngleDegrees - 90.0)).toFloat()
                val sweepEnd = Offset(
                    center.x + maxRadius * Math.cos(sweepRad.toDouble()).toFloat(),
                    center.y + maxRadius * Math.sin(sweepRad.toDouble()).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(NeonGreen.copy(alpha = 0.9f), Color.Transparent),
                        start = center,
                        end = sweepEnd
                    ),
                    start = center,
                    end = sweepEnd,
                    strokeWidth = 3f
                )

                // Plot Tactical Markers
                state.markers.forEach { marker ->
                    // Map xOffsetPx, yOffsetPx to canvas scale
                    val scale = maxRadius / 280f
                    val markerPos = Offset(
                        center.x + (marker.xOffsetPx * scale),
                        center.y + (marker.yOffsetPx * scale)
                    )

                    val markerColor = when (marker.type) {
                        MarkerType.TRAINING_TARGET -> NeonRed
                        MarkerType.TEAM_MATE -> NeonCyan
                        MarkerType.OBJECTIVE_DROP -> NeonYellow
                    }

                    // Outer dot glow
                    drawCircle(
                        color = markerColor.copy(alpha = 0.4f),
                        radius = 8f,
                        center = markerPos
                    )
                    // Inner solid marker
                    drawCircle(
                        color = markerColor,
                        radius = 4.5f,
                        center = markerPos
                    )
                }

                // Self Player Marker (Center)
                drawCircle(
                    color = NeonGreen,
                    radius = 5.5f,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = center
                )
            }
        }

        // Tactical Marker List Feed
        Text(
            text = "TACTICAL TELEMETRY FEED (100M RANGE)",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.markers) { marker ->
                val color = when (marker.type) {
                    MarkerType.TRAINING_TARGET -> NeonRed
                    MarkerType.TEAM_MATE -> NeonCyan
                    MarkerType.OBJECTIVE_DROP -> NeonYellow
                }

                GlassCard(
                    borderColor = color.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = marker.label,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = marker.type.name.replace("_", " "),
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format("%.1f", marker.distanceMeters)}m",
                                color = color,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${String.format("%.0f", marker.bearingDegrees)}° BRG",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}
