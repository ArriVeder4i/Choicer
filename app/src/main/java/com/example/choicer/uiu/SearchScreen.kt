package com.example.choicer.uiu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie
import com.example.choicer.viewmodel.MovieViewModel
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    var showFilters by remember { mutableStateOf(false) }

    val selectedGenres by viewModel.selectedGenres.collectAsState()
    val ratingRange by viewModel.ratingRange.collectAsState()
    val yearRange by viewModel.yearRange.collectAsState()

    var suggestions by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    BluredGradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query

                            searchJob?.cancel()
                            searchJob = scope.launch {
                                delay(300)

                                if (query.isNotBlank()) {
                                    viewModel.searchMovies(query)
                                    suggestions = viewModel.searchResults.value.take(5)
                                } else {
                                    suggestions = emptyList()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Найти фильм...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            focusedContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.searchMovies(searchQuery)
                                suggestions = emptyList()
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    AnimatedVisibility(visible = suggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.9f)
                            )
                        ) {
                            LazyColumn {
                                items(suggestions) { movie ->
                                    Text(
                                        text = movie.title,
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchQuery = movie.title
                                                suggestions = emptyList()
                                                viewModel.searchMovies(movie.title)
                                                keyboardController?.hide()
                                            }
                                            .padding(12.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        null,
                        tint = if (showFilters) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }

            AnimatedVisibility(visible = showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text("Жанры:", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            viewModel.genresList.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { genre ->
                                        val isSelected = selectedGenres.any { it.id == genre.id }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 4.dp)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                    else Color.White.copy(alpha = 0.05f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else Color.White.copy(alpha = 0.1f),
                                                    RoundedCornerShape(8.dp)
                                                )
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
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Годы: ${yearRange.start.toInt()} - ${yearRange.endInclusive.toInt()}", color = Color.White)
                        RangeSlider(
                            value = yearRange,
                            onValueChange = { viewModel.yearRange.value = it },
                            valueRange = 1970f..2025f
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Рейтинг: ${String.format("%.1f", ratingRange.start)} - ${String.format("%.1f", ratingRange.endInclusive)}",
                            color = Color.White
                        )
                        RangeSlider(
                            value = ratingRange,
                            onValueChange = { viewModel.ratingRange.value = it },
                            valueRange = 0f..10f
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.searchMovies(searchQuery)
                                suggestions = emptyList()
                                showFilters = false
                                keyboardController?.hide()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Применить фильтры")
                        }
                    }
                }
            }

            if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ничего не найдено 🕵️‍♂️", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { movie ->
                        SearchMovieCard(movie, viewModel, wishlist, onNavigateToDetails)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchMovieCard(
    movie: Movie,
    viewModel: MovieViewModel,
    wishlist: List<Movie>,
    onNavigateToDetails: () -> Unit
) {
    val isLiked = wishlist.any { it.id == movie.id }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel.selectedMovieForDetails.value = movie
                viewModel.loadExtraDetails(movie.id)
                onNavigateToDetails()
            },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column {
            Box {
                AsyncImage(
                    model = movie.posterUrl(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = {
                        if (isLiked) viewModel.removeFromWishlist(movie)
                        else viewModel.addToWishlist(movie)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(bottomStart = 8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        null,
                        tint = if (isLiked) Color.Red else Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .height(80.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${movie.formattedRating}", color = Color.Yellow, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        movie.releaseYear.ifBlank { "Н/Д" },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
