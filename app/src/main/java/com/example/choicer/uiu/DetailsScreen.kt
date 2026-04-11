package com.example.choicer.uiu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.choicer.viewmodel.MovieViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(viewModel: MovieViewModel, onBackClick: () -> Unit) {
    val movie = viewModel.selectedMovieForDetails.value
    val context = LocalContext.current
    val description by viewModel.detailedDescription
    val genres by viewModel.movieGenres
    val actors by viewModel.movieActors

    if (movie == null) { onBackClick(); return }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О фильме", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
                    }
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
            // Постер (теперь НЕ кликабельный и без надписей)
            Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                AsyncImage(
                    model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path
                    else "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(450.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = movie.title, style = MaterialTheme.typography.headlineLarge, color = Color.White)

                if (genres.isNotEmpty()) {
                    Text(text = genres, color = Color.Cyan, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐ ${if (movie.vote_average == 0.0) "-" else String.format("%.1f", movie.vote_average)}", color = Color.Yellow, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "📅 ${movie.release_date?.take(4) ?: "Н/Д"}", color = Color.LightGray, style = MaterialTheme.typography.titleMedium)
                    // ID УБРАН ОТСЮДА
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.addToWishlist(movie) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Добавить в избранное") }

                Spacer(modifier = Modifier.height(24.dp))

                if (actors.isNotEmpty()) {
                    Text("В главных ролях", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        items(actors) { actor ->
                            Column(modifier = Modifier.width(90.dp).padding(end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(model = actor.posterUrl, contentDescription = null, modifier = Modifier.size(70.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                Text(actor.nameRu ?: "", color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Описание", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(description, color = Color.LightGray, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}