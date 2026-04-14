package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.choicer.viewmodel.MovieViewModel

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun DetailsScreen(viewModel: MovieViewModel, onBackClick: () -> Unit) {

    val movie = viewModel.selectedMovieForDetails.value
    val description by viewModel.detailedDescription
    val genres by viewModel.movieGenres
    val actors by viewModel.movieActors
    val wishlist by viewModel.wishlist.collectAsState()

    val activeVideoId by viewModel.activeVideoId
    val currentVideoUrl by viewModel.currentVideoUrl

    if (movie == null) {
        onBackClick()
        return
    }

    val isLiked = wishlist.any { it.id == movie.id }
    val hasClip = viewModel.hasClip(movie.id)

    // 🔥 ЕСЛИ ВИДЕО — ПОКАЗЫВАЕМ ТОЛЬКО ПЛЕЕР
    if (activeVideoId == movie.id && currentVideoUrl != null) {
        VideoPlayer(
            url = currentVideoUrl!!,
            onBack = {
                viewModel.isVideoMode.value = false
                viewModel.activeVideoId.value = null
            }
        )
        return
    }

    // 🔥 ОБЫЧНЫЙ ЭКРАН
    BluredGradientBackground(noPadding = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { AppScreenTitle(text = "О фильме") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {

                // 🎬 ПОСТЕР
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .clickable {
                            if (hasClip) {
                                viewModel.toggleVideoMode(movie)
                            }
                        }
                ) {

                    AsyncImage(
                        model = movie.posterUrl(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (hasClip) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(90.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    if (genres.isNotEmpty()) {
                        Text(
                            text = genres,
                            color = Color.Cyan,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ⭐ + 📅 + ❤️
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                text = "⭐ ${movie.formattedRating}",
                                color = Color.Yellow,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "📅 ${movie.releaseYear.ifBlank { "Н/Д" }}",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isLiked) viewModel.removeFromWishlist(movie)
                                else viewModel.addToWishlist(movie)
                            },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (isLiked) Color.Red else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (actors.isNotEmpty()) {
                        Text("В главных ролях", style = MaterialTheme.typography.titleLarge, color = Color.White)

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            items(actors) { actor ->
                                Column(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .padding(end = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = actor.posterUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        actor.nameRu ?: "",
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Описание", style = MaterialTheme.typography.titleLarge, color = Color.White)

                    Text(
                        description,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
