package com.example.choicer.uiu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.Segment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.choicer.data.PosterSize
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onNavigateToDetails: () -> Unit
) {
    val movies by viewModel.trendingMovies.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()

    val isVideoMode by viewModel.isVideoMode
    val activeVideoId by viewModel.activeVideoId
    val isVideoLoading by viewModel.isVideoLoading
    val currentVideoUrl by viewModel.currentVideoUrl

    val pagerState = rememberPagerState(pageCount = { movies.size })

    // 🔥 фикс пагинации
    var lastRequestedPage by remember { mutableStateOf(-1) }

    // 1. Оставляем здесь ТОЛЬКО пагинацию (подгрузку новых фильмов)
    LaunchedEffect(pagerState.currentPage) {
        val page = pagerState.currentPage
        if (movies.isNotEmpty() && page >= movies.size - 5 && lastRequestedPage != page) {
            lastRequestedPage = page
            viewModel.getNextPage()
        }
    }

// 2. ДОБАВЛЯЕМ НОВЫЙ ЭФФЕКТ: Сброс видео при НАЧАЛЕ скролла
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress && viewModel.isVideoMode.value) {
            viewModel.isVideoMode.value = false
            viewModel.activeVideoId.value = null
            viewModel.currentVideoUrl.value = null
        }
    }

    BluredGradientBackground(noPadding = true) {

        Box(modifier = Modifier.fillMaxSize()) {

            if (movies.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->

                    val movie = movies[page]
                    val hasClip = viewModel.hasClip(movie.id)

                    val isLiked = wishlist.any { it.id == movie.id }

                    Box(modifier = Modifier.fillMaxSize()) {

                        if (activeVideoId == movie.id && currentVideoUrl != null) {
                            VideoPlayer(
                                url = currentVideoUrl!!,
                                onBack = {
                                    viewModel.isVideoMode.value = false
                                    viewModel.activeVideoId.value = null
                                }
                            )
                        } else {

                            AsyncImage(
                                model = movie.posterUrl(PosterSize.ORIGINAL),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        if (hasClip) {
                                            viewModel.toggleVideoMode(movie)
                                        } else {
                                            viewModel.selectedMovieForDetails.value = movie
                                            viewModel.loadExtraDetails(movie.id)
                                            onNavigateToDetails()
                                        }
                                    },
                                alignment = Alignment.Center,
                                contentScale = ContentScale.Crop
                            )

                            if (hasClip) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(80.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }

                        if (activeVideoId != movie.id) {

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = 80.dp,
                                        start = 16.dp,
                                        end = 16.dp
                                    ),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = movie.title,
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "⭐ ${movie.formattedRating}",
                                        color = Color.Yellow,
                                        fontSize = 16.sp
                                    )
                                }

                                Column {

                                    FloatingActionButton(
                                        onClick = {
                                            viewModel.selectedMovieForDetails.value = movie
                                            viewModel.loadExtraDetails(movie.id)
                                            onNavigateToDetails()
                                        },
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Segment, null)
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    FloatingActionButton(
                                        onClick = {
                                            if (isLiked) viewModel.removeFromWishlist(movie)
                                            else viewModel.addToWishlist(movie)
                                        },
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = if (isLiked) Color.Red else Color.White
                                    ) {
                                        Icon(Icons.Default.Favorite, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isVideoLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }
}
