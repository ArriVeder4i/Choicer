package com.example.choicer.uiu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie
import com.example.choicer.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    val selectedGenres by viewModel.selectedGenres.collectAsState()
    val ratingRange by viewModel.ratingRange.collectAsState()
    val yearRange by viewModel.yearRange.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Поисковая строка
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Найти фильм...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Default.List, contentDescription = "Фильтры", tint = if (showFilters) MaterialTheme.colorScheme.primary else Color.White)
            }
        }

        // Панель фильтров
        AnimatedVisibility(visible = showFilters) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Жанры:", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    // ИСПРАВЛЕННАЯ СЕТКА ЖАНРОВ
                    Column {
                        viewModel.genresList.chunked(2).forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { genre ->
                                    val isSelected = selectedGenres.any { it.id == genre.id }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f) // Каждый занимает ровно половину
                                            .padding(vertical = 4.dp)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.toggleGenre(genre) }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (if (isSelected) "✓ " else "") + genre.name,
                                            color = if (isSelected) Color.White else Color.Gray,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                // Если в паре только один элемент, добавляем пустой вес, чтобы кнопка не растягивалась
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Годы: ${yearRange.start.toInt()} - ${yearRange.endInclusive.toInt()}", color = Color.White)
                    RangeSlider(value = yearRange, onValueChange = { viewModel.yearRange.value = it }, valueRange = 1970f..2025f, steps = 55)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Рейтинг: ${String.format("%.1f", ratingRange.start)} - ${String.format("%.1f", ratingRange.endInclusive)}", color = Color.White)
                    RangeSlider(value = ratingRange, onValueChange = { viewModel.ratingRange.value = it }, valueRange = 0f..10f, steps = 100)

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.searchMovies(searchQuery); showFilters = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Применить фильтры")
                    }
                }
            }
        }

        // РЕЗУЛЬТАТЫ ИЛИ УВЕДОМЛЕНИЕ
        if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Ничего не найдено 🕵️‍♂️\nПопробуйте изменить фильтры",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchResults) { movie ->
                    SearchMovieCard(movie = movie, viewModel = viewModel, onNavigateToDetails = onNavigateToDetails)
                }
            }
        }
    }
}

@Composable
fun SearchMovieCard(movie: Movie, viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            viewModel.selectedMovieForDetails.value = movie
            viewModel.loadExtraDetails(movie.id)
            onNavigateToDetails()
        },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Box {
                AsyncImage(
                    model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path else "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { viewModel.addToWishlist(movie) },
                    modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 8.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.Green)
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = movie.title, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐ ${movie.formattedRating}", color = Color.Yellow, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = movie.release_date?.take(4) ?: "Н/Д", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}