package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AddonBanner(
    bannerType: String,
    modifier: Modifier = Modifier
) {
    // Pulse animation for animated redstone glow or portal swirls
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val portalOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portalOffset"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (bannerType.lowercase()) {
                "creeper" -> {
                    // Green grass/creeper background with pixel textures
                    drawRect(color = Color(0xFF4CAF50), size = size)
                    
                    // Minecraft pixel noise
                    val pxSize = w / 16f
                    for (x in 0 until 16) {
                        for (y in 0 until 16) {
                            if ((x + y) % 3 == 0) {
                                drawRect(
                                    color = Color(0xFF388E3C),
                                    topLeft = Offset(x * pxSize, y * pxSize),
                                    size = Size(pxSize, pxSize)
                                )
                            }
                            if ((x - y) % 5 == 0) {
                                drawRect(
                                    color = Color(0xFF66BB6A),
                                    topLeft = Offset(x * pxSize, y * pxSize),
                                    size = Size(pxSize, pxSize)
                                )
                            }
                        }
                    }

                    // Creeper face in center (scaled appropriately)
                    // Grid size is roughly 8x8 in typical block. Let's draw centered.
                    // Grid mapping:
                    // (2,2) and (5,2) are eyes
                    // (3,3) is nose center, with lower details
                    val cell = w / 10f
                    val offsetX = (w - (cell * 8f)) / 2f
                    val offsetY = (h - (cell * 8f)) / 2f

                    // Color of creeper mask: Black
                    val eyeColor = Color(0xFF1B1B1B)
                    
                    // Left Eye (columns 1,2 - row 1,2 from offset)
                    drawRect(eyeColor, topLeft = Offset(offsetX + cell * 1, offsetY + cell * 1), size = Size(cell * 2, cell * 2))
                    // Right Eye (columns 5,6 - row 1,2 from offset)
                    drawRect(eyeColor, topLeft = Offset(offsetX + cell * 5, offsetY + cell * 1), size = Size(cell * 2, cell * 2))
                    // Nose (columns 3,4 - row 3,4,5)
                    drawRect(eyeColor, topLeft = Offset(offsetX + cell * 3, offsetY + cell * 3), size = Size(cell * 2, cell * 3))
                    // Left mouth branch (column 2 - row 4,5,6)
                    drawRect(eyeColor, topLeft = Offset(offsetX + cell * 2, offsetY + cell * 4), size = Size(cell * 1, cell * 3))
                    // Right mouth branch (column 5 - row 4,5,6)
                    drawRect(eyeColor, topLeft = Offset(offsetX + cell * 5, offsetY + cell * 4), size = Size(cell * 1, cell * 3))
                }

                "dirt" -> {
                    // Soil brown background
                    drawRect(color = Color(0xFF5C4033), size = size)
                    
                    // Pixelated brown terrain
                    val pxSize = w / 14f
                    for (x in 0..14) {
                        for (y in 0..14) {
                            if ((x * y + x) % 4 == 0) {
                                drawRect(
                                    color = Color(0xFF4E3629),
                                    topLeft = Offset(x * pxSize, y * pxSize),
                                    size = Size(pxSize, pxSize)
                                )
                            }
                            if ((x * 3 + y * 7) % 5 == 0) {
                                drawRect(
                                    color = Color(0xFF704F3F),
                                    topLeft = Offset(x * pxSize, y * pxSize),
                                    size = Size(pxSize, pxSize)
                                )
                            }
                        }
                    }

                    // Green turf grass layer at top
                    val grassHeight = h * 0.35f
                    drawRect(
                        color = Color(0xFF4CAF50),
                        size = Size(w, grassHeight)
                    )

                    // Jagged pixel roots extending down (dirt grass interface)
                    for (x in 0..10) {
                        val rootLen = if (x % 2 == 0) pxSize * 1.5f else pxSize * 2.5f
                        drawRect(
                            color = Color(0xFF388E3C),
                            topLeft = Offset(x * (w / 10f), grassHeight - 2f),
                            size = Size(w / 10f, rootLen)
                        )
                    }
                }

                "sword" -> {
                    // Dark steel gradient
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2C3E50), Color(0xFF0F2027))
                        ),
                        size = size
                    )

                    // Draw classic pixelated diagonal diamond sword
                    val bladeColor = Color(0xFF00E5FF) // Diamondcyan glow!
                    val bladeBorder = Color(0xFF006064)
                    val handleColor = Color(0xFF5D4033) // wood brown

                    val numPixels = 12
                    val pxWidth = w / 16f
                    val pxHeight = h / 16f

                    // Draw diagonal blade
                    for (i in 0 until numPixels) {
                        val row = 14 - i
                        val col = 2 + i

                        // Border
                        drawRect(
                            color = bladeBorder,
                            topLeft = Offset(col * pxWidth, row * pxHeight),
                            size = Size(pxWidth * 1.3f, pxHeight * 1.3f)
                        )
                        // Inner blade
                        drawRect(
                            color = if (i < 3) handleColor else bladeColor,
                            topLeft = Offset(col * pxWidth + 2f, row * pxHeight + 2f),
                            size = Size(pxWidth * 0.9f, pxHeight * 0.9f)
                        )
                    }

                    // Add dynamic iron details on remaining parts
                    drawRect(
                        color = Color(0xFFFFD700).copy(alpha = pulseAlpha), // Golden spark
                        topLeft = Offset(w * 0.7f, h * 0.2f),
                        size = Size(pxWidth * 1.2f, pxHeight * 1.2f)
                    )
                }

                "portal" -> {
                    // Obsidian frame and glowing purple void
                    drawRect(color = Color(0xFF14101A), size = size)

                    // Draw swirling portal layers
                    val steps = 8
                    for (i in 0 until steps) {
                        val scale = 1f - (i.toFloat() / steps.toFloat())
                        val swirlW = w * scale
                        val swirlH = h * scale
                        val purpleHue = Color(0xFF7B1FA2).copy(alpha = (0.2f + 0.15f * i) * pulseAlpha)
                        
                        drawRect(
                            color = purpleHue,
                            topLeft = Offset((w - swirlW) / 2f + (portalOffset * (i % 2 - 0.5f)), (h - swirlH) / 2f),
                            size = Size(swirlW, swirlH)
                        )
                    }
                    
                    // Outer black obsidian block frame accents
                    val thick = w * 0.12f
                    drawRect(color = Color(0xFF0C0712), size = Size(thick, h)) // Left
                    drawRect(color = Color(0xFF0C0712), topLeft = Offset(w - thick, 0f), size = Size(thick, h)) // Right
                    drawRect(color = Color(0xFF0C0712), size = Size(w, thick)) // Top
                    drawRect(color = Color(0xFF0C0712), topLeft = Offset(0f, h - thick), size = Size(w, thick)) // Bottom
                }

                "water" -> {
                    // Oceanic deep water background
                    drawRect(color = Color(0xFF01579B), size = size)
                    
                    // Wave lines
                    val pxSize = w / 16f
                    for (row in 0..16) {
                        for (col in 0..16) {
                            if ((row + col) % 4 == 0) {
                                drawRect(
                                    color = Color(0xFF039BE5).copy(alpha = pulseAlpha),
                                    topLeft = Offset(col * pxSize, row * pxSize),
                                    size = Size(pxSize * 1.4f, pxSize * 0.5f)
                                )
                            }
                            if ((row - col) % 6 == 0) {
                                drawRect(
                                    color = Color(0xFFE0F7FA).copy(alpha = 0.3f),
                                    topLeft = Offset(col * pxSize, row * pxSize),
                                    size = Size(pxSize, pxSize)
                                )
                            }
                        }
                    }
                }

                "gold" -> {
                    // Rich gold checkered blocks
                    drawRect(color = Color(0xFFFFB300), size = size)
                    
                    val px = w / 12f
                    for (x in 0..12) {
                        for (y in 0..12) {
                            if ((x + y) % 3 == 0) {
                                drawRect(
                                    color = Color(0xFFFFD54F),
                                    topLeft = Offset(x * px, y * px),
                                    size = Size(px, px)
                                )
                            }
                            if ((x * 2 - y) % 5 == 0) {
                                drawRect(
                                    color = Color(0xFFFF8F00),
                                    topLeft = Offset(x * px, y * px),
                                    size = Size(px, px)
                                )
                            }
                        }
                    }
                }

                "redstone" -> {
                    // Redstone block style
                    drawRect(color = Color(0xFF8B0000), size = size)

                    val px = w / 15f
                    for (x in 0..15) {
                        for (y in 0..15) {
                            if ((x + y) % 3 == 0) {
                                drawRect(
                                    color = Color(0xFFB22222),
                                    topLeft = Offset(x * px, y * px),
                                    size = Size(px, px)
                                )
                            }
                            // Glowing dots
                            if ((x * 3 + y) % 7 == 0) {
                                drawRect(
                                    color = Color(0xFFFF4500).copy(alpha = pulseAlpha),
                                    topLeft = Offset(x * px, y * px),
                                    size = Size(px * 0.8f, px * 0.8f)
                                )
                            }
                        }
                    }
                }

                else -> {
                    // Default Stone look
                    drawRect(color = Color(0xFF9E9E9E), size = size)
                    val px = w / 10f
                    for (x in 0..10) {
                        for (y in 0..10) {
                            if ((x + y) % 2 == 0) {
                                drawRect(
                                    color = Color(0xFF757575),
                                    topLeft = Offset(x * px, y * px),
                                    size = Size(px, px)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
