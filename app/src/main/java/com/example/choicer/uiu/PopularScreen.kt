package com.example.choicer.uiu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun PopularScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    val ruMovies by viewModel.popularRuMovies.collectAsState()
    val ruSeries by viewModel.popularRuSeries.collectAsState()
    val foreignMovies by viewModel.popularForeignMovies.collectAsState()
    val foreignSeries by viewModel.popularForeignSeries.collectAsState()

    BluredGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            AppScreenTitle(
                text = "Популярное",
                modifier = Modifier.padding(16.dp)
            )

            // 4 крутилки
            PopularSection("Зарубежные фильмы", foreignMovies, viewModel, onNavigateToDetails)
            PopularSection("Зарубежные сериалы", foreignSeries, viewModel, onNavigateToDetails)
            PopularSection("Российские фильмы", ruMovies, viewModel, onNavigateToDetails)
            PopularSection("Российские сериалы", ruSeries, viewModel, onNavigateToDetails)
        }
    }
}
@Composable
fun PopularSection(title: String, movies: List<Movie>, viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    if (movies.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies) { movie ->
                Column(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable {
                            viewModel.selectedMovieForDetails.value = movie
                            viewModel.loadExtraDetails(movie.id)
                            onNavigateToDetails()
                        }
                ) {
                    AsyncImage(
                        model = movie.posterUrl(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "⭐ ${movie.formattedRating}",
                        color = Color.Yellow,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
