package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Segment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun HomeScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    val movies by viewModel.trendingMovies.collectAsState()
    val videoUrl by viewModel.currentVideoUrl
    val isVideoLoading = viewModel.isVideoLoading.value

    val pagerState = rememberPagerState(pageCount = { movies.size })

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (movies.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val movie = movies[page]
                val hasClip = viewModel.hasClip(movie.id)

                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path
                        else "https://image.tmdb.org/t/p/original${movie.poster_path}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clickable { viewModel.loadVideo(movie) },
                        contentScale = ContentScale.Crop
                    )

                    if (hasClip) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(64.dp).align(Alignment.Center)
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)), startY = 500f)))

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(movie.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("⭐ ${movie.formattedRating}", color = Color.Yellow, fontSize = 16.sp)
                        }
                        Column {
                            // ИКОНКА О ФИЛЬМЕ (поменял на Segment/Notes)
                            FloatingActionButton(
                                onClick = {
                                    viewModel.selectedMovieForDetails.value = movie
                                    viewModel.loadExtraDetails(movie.id)
                                    onNavigateToDetails()
                                },
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ) { Icon(Icons.Default.Segment, null) }

                            Spacer(Modifier.height(16.dp))

                            // ИКОНКА ДОБАВИТЬ В ВИШЛИСТ (поменял на Сердце)
                            FloatingActionButton(
                                onClick = { viewModel.addToWishlist(movie) },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ) { Icon(Icons.Default.Favorite, null) }
                        }
                    }
                }
            }
        }

        if (videoUrl != null) {
            VideoScreen(url = videoUrl!!, onBack = { viewModel.currentVideoUrl.value = null })
        }

        if (isVideoLoading) Dialog(onDismissRequest = {}) { CircularProgressIndicator(color = Color.White) }
    }
}