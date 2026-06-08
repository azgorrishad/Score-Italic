package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val color: Color,
    val speedX: Float,
    var speedY: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val width: Float,
    val height: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val particles = remember { mutableStateListOf<ConfettiParticle>() }

    // Colors of the Italian Flag + Golden celebration
    val colors = listOf(
        Color(0xFF008C45), // Green
        Color(0xFFF4F9FF), // White
        Color(0xFFCD212A), // Red
        Color(0xFFD4AF37)  // Gold
    )

    LaunchedEffect(Unit) {
        // Initialize particles
        for (i in 0..80) {
            particles.add(
                ConfettiParticle(
                    x = Random.nextFloat() * 1000f,
                    y = -Random.nextFloat() * 800f,
                    color = colors[Random.nextInt(colors.size)],
                    speedX = Random.nextFloat() * 4f - 2f,
                    speedY = Random.nextFloat() * 5f + 4f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 4f - 2f,
                    width = Random.nextFloat() * 10f + 12f,
                    height = Random.nextFloat() * 6f + 8f
                )
            )
        }

        // Animation frame loop
        while (true) {
            withFrameMillis { _ ->
                for (i in particles.indices) {
                    val p = particles[i]
                    p.y += p.speedY
                    p.x += p.speedX
                    p.rotation += p.rotationSpeed

                    // Recycle particle if it falls off bottom edge
                    if (p.y > 2200f) {
                        p.y = -50f
                        p.x = Random.nextFloat() * 1080f
                        p.speedY = Random.nextFloat() * 5f + 4f
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleX = size.width / 1000f // dynamically adapt custom width grid to real Canvas boundaries
        particles.forEach { p ->
            withTransform({
                rotate(p.rotation, pivot = Offset(p.x * scaleX, p.y))
            }) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x * scaleX - p.width / 2, p.y - p.height / 2),
                    size = Size(p.width, p.height)
                )
            }
        }
    }
}
