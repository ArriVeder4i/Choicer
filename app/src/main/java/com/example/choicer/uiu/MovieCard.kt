package com.example.choicer.uiu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie

@Composable
fun MovieWishlistCard(
    movie: Movie,
    onWatchedClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val cardAlpha = if (movie.isWatched) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(bottom = 8.dp)
            .alpha(cardAlpha),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path else "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                modifier = Modifier.width(70.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = movie.title ?: "Без названия",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Используем свойство из модели
                Text(text = "⭐ ${movie.formattedRating}", color = Color.Yellow)
            }

            if (onWatchedClick != null) {
                IconButton(onClick = onWatchedClick) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Просмотрено",
                        tint = if (movie.isWatched) Color.Green else Color.LightGray
                    )
                }
            }

            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}