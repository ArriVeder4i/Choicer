package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun SearchScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newText ->
                searchQuery = newText
                if (newText.length > 2) viewModel.searchMovies(newText)
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Найти фильм...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            )
        )

        if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ничего не найдено 🕵️‍♂️", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)) {
                items(searchResults) { movie ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 8.dp)
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
                                Text(movie.title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))

                                val year = movie.release_date?.take(4) ?: "Н/Д"
                                // Используем свойство из модели
                                Text(text = "⭐ ${movie.formattedRating}  📅 $year", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }

                            IconButton(
                                onClick = { viewModel.addToWishlist(movie) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.Green)
                            }
                        }
                    }
                }
            }
        }
    }
}