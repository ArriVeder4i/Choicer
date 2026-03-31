package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun WishlistScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    val wishlist by viewModel.wishlist.collectAsState()

    var showRandomDialog by remember { mutableStateOf(false) }
    var selectedRandomMovie by remember { mutableStateOf<Movie?>(null) }

    val unwatchedMovies = wishlist.filter { !it.isWatched }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (wishlist.isEmpty()) {
            Text(
                text = "Ваш список пуст 🍿\nДобавьте фильмы на главном",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                items(wishlist) { movie ->
                    val cardAlpha = if (movie.isWatched) 0.5f else 1f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 8.dp)
                            .alpha(cardAlpha)
                            .clickable {
                                viewModel.selectedMovieForDetails.value = movie
                                viewModel.loadExtraDetails(movie.id)
                                onNavigateToDetails()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path else "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                                contentDescription = null,
                                modifier = Modifier.width(70.dp).fillMaxHeight(),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier.weight(1f).padding(12.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = movie.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Используем свойство из модели
                                Text(text = "⭐ ${movie.formattedRating}", color = Color.Yellow)
                            }

                            IconButton(onClick = { viewModel.toggleWatchedStatus(movie) }) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Просмотрено",
                                    tint = if (movie.isWatched) Color.Green else Color.LightGray
                                )
                            }

                            IconButton(onClick = { viewModel.removeFromWishlist(movie) }) {
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
        }

        if (unwatchedMovies.isNotEmpty()) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 60.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    selectedRandomMovie = unwatchedMovies.random()
                    showRandomDialog = true
                }
            ) {
                Text("🎲", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }

    if (showRandomDialog && selectedRandomMovie != null) {
        AlertDialog(
            onDismissRequest = { showRandomDialog = false },
            title = { Text("Случайный выбор!") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = if (selectedRandomMovie!!.poster_path?.startsWith("http") == true) selectedRandomMovie!!.poster_path else "https://image.tmdb.org/t/p/w500${selectedRandomMovie!!.poster_path}",
                        contentDescription = null,
                        modifier = Modifier.height(200.dp).padding(bottom = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text(selectedRandomMovie!!.title, style = MaterialTheme.typography.headlineSmall)
                }
            },
            confirmButton = {
                Button(onClick = { showRandomDialog = false }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.toggleWatchedStatus(selectedRandomMovie!!)
                    showRandomDialog = false
                }) {
                    Text("Я уже смотрел", color = Color.Gray)
                }
            }
        )
    }
}