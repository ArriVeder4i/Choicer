package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BluredGradientBackground(
    noPadding: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {

        Box(modifier = Modifier.fillMaxSize().blur(100.dp)) {

            Spacer(
                modifier = Modifier
                    .size(350.dp)
                    .offset(x = (-50).dp, y = (-80).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1A237E), Color.Transparent)
                        )
                    )
            )

            Spacer(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 100.dp, y = 100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF4A148C), Color.Transparent)
                        )
                    )
            )

            Spacer(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = (50).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF006064), Color.Transparent)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (noPadding) Modifier
                    else Modifier.padding(16.dp)
                )
        ) {
            content()
        }
    }
}