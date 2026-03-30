package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.choicer.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(viewModel: MovieViewModel, onBackClick: () -> Unit) {
    val movie = viewModel.selectedMovieForDetails.value

    // Подтягиваем новые данные из ViewModel
    val description by viewModel.detailedDescription
    val genres by viewModel.movieGenres
    val actors by viewModel.movieActors

    if (movie == null) {
        onBackClick()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О фильме", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Постер
            AsyncImage(
                model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path else "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(450.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Название
                Text(text = movie.title, style = MaterialTheme.typography.headlineLarge, color = Color.White)

                // Жанры (НОВОЕ)
                if (genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = genres, color = Color.Cyan, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Рейтинг и Год
                val year = movie.release_date?.take(4) ?: "Н/Д"
                val ratingText = if (movie.vote_average == 0.0 || movie.vote_average == null) "-" else String.format("%.1f", movie.vote_average)

                Row {
                    Text(text = "⭐ $ratingText", color = Color.Yellow, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "📅 $year", color = Color.LightGray, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.addToWishlist(movie) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Добавить в мой список")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // АКТЕРЫ (НОВОЕ)
                if (actors.isNotEmpty()) {
                    Text(text = "В главных ролях", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                        items(actors) { actor ->
                            Column(
                                modifier = Modifier.width(90.dp).padding(end = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = actor.posterUrl,
                                    contentDescription = actor.nameRu,
                                    modifier = Modifier.size(70.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = actor.nameRu ?: "",
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Описание (НОВОЕ)
                Text(text = "Описание", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}