package com.example.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassyPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val targetBorderColor = if (borderGlow) NeonCyan.copy(alpha = 0.6f) else GlassBorder
    val borderStrokeWidth = if (borderGlow) 1.5.dp else 1.dp

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x33FFFFFF), // Frosted white glaze at top
                        Color(0x15FFFFFF), // Translucent midpoint
                        Color(0x0A000000)  // Deep shadow at bottom
                    )
                )
            )
            .border(
                BorderStroke(borderStrokeWidth, targetBorderColor),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Blur background placeholder effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
                .background(Color(0x1A000000))
        )
        // Content container
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0x1A080B10))
                .padding(12.dp)
        ) {
            content()
        }
    }
}

@Composable
fun GlassyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glow: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val borderBrush = Brush.verticalGradient(
        colors = if (glow) {
            listOf(Color(0xFFE0F7FA), NeonCyan, NeonBlue)
        } else {
            listOf(Color(0x80FFFFFF), Color(0x20FFFFFF), Color(0x05FFFFFF))
        }
    )

    val backgroundColors = if (glow) {
        Brush.verticalGradient(
            colors = listOf(
                NeonCyan.copy(alpha = 0.35f),
                NeonBlue.copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x25FFFFFF),
                Color(0x08FFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColors)
            .border(BorderStroke(1.dp, borderBrush), shape = RoundedCornerShape(24.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
fun GlassyIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activated: Boolean = false,
    glowColor: Color = NeonCyan,
    size: Dp = 48.dp
) {
    val scale by animateFloatAsState(targetValue = if (activated) 1.1f else 1.0f, label = "button_scale")
    val borderThickness by animateDpAsState(targetValue = if (activated) 1.5.dp else 1.dp, label = "border_thickness")

    val bgBrush = if (activated) {
        Brush.verticalGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.4f),
                glowColor.copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x1FFFFFFF),
                Color(0x06FFFFFF)
            )
        )
    }

    val finalBorderColor = if (activated) glowColor else Color(0x3DFFFFFF)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgBrush)
            .border(BorderStroke(borderThickness, finalBorderColor), shape = CircleShape)
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.5f),
            tint = if (activated) glowColor else Color.White
        )
    }
}

@Composable
fun GlassySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    accentColor: Color = NeonCyan
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = modifier,
        colors = SliderDefaults.colors(
            activeTrackColor = accentColor,
            inactiveTrackColor = Color(0x33FFFFFF),
            thumbColor = Color.White,
            activeTickColor = accentColor.copy(alpha = 0.5f),
            inactiveTickColor = Color(0x1AFFFFFF)
        )
    )
}

@Composable
fun TactileDial(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Color(0x1F000000))
            .border(BorderStroke(1.dp, Color(0x1AFFFFFF)), shape = RoundedCornerShape(27.dp))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val textColor = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.6f)
                val textBg = if (isSelected) Color(0x2600E5FF) else Color.Transparent

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(textBg)
                        .clickable(
                            onClick = { onItemSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                }
            }
        }
    }
}
