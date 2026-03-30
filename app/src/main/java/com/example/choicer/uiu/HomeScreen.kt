package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun HomeScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    val movies by viewModel.trendingMovies.collectAsState()
    val pagerState = rememberPagerState(pageCount = { movies.size })

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (movies.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val movie = movies[page]

                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path else "https://image.tmdb.org/t/p/original${movie.poster_path}",
                        contentDescription = movie.title ?: "Постер",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 500f
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title ?: "Без названия",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⭐ ${movie.vote_average ?: 0.0}  |  ${movie.release_date ?: "Н/Д"}",
                                color = Color.Yellow,
                                fontSize = 16.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = {
                                    viewModel.selectedMovieForDetails.value = movie
                                    viewModel.loadExtraDetails(movie.id)
                                    onNavigateToDetails()
                                },
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Инфо")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            FloatingActionButton(
                                onClick = { viewModel.addToWishlist(movie) },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить")
                            }
                        }
                    }
                }
            }
        }
    }
}