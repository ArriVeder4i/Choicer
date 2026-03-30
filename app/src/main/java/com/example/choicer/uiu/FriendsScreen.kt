package com.example.choicer.uiu

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun FriendsScreen(viewModel: MovieViewModel) {
    val wishlist by viewModel.wishlist.collectAsState()

    val isConnected by viewModel.isConnected.collectAsState()
    val matchedItems by viewModel.matchedMovies.collectAsState() // Совпадения
    val combinedItems by viewModel.combinedMovies.collectAsState() // Все вместе

    var isQrVisible by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    var showRandomDialog by remember { mutableStateOf(false) }
    var randomSelectedMovie by remember { mutableStateOf<Movie?>(null) }

    // НОВОЕ СОСТОЯНИЕ: Какая вкладка выбрана (0 - Совпадения, 1 - Общий)
    var selectedTabIndex by remember { mutableStateOf(0) }

    if (showScanner) {
        Box(modifier = Modifier.fillMaxSize()) {
            QrScannerView(
                onCodeScanned = { scannedData ->
                    showScanner = false
                    viewModel.matchWishlists(scannedData)
                }
            )

            IconButton(
                onClick = { showScanner = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть сканер", tint = Color.White)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Друзья и Совпадения 🤝 👓",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isConnected) {
                // ДО КОННЕКТА
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { isQrVisible = !isQrVisible }) {
                        Text(if (isQrVisible) "Скрыть мой QR" else "Показать мой QR")
                    }
                    Button(onClick = { showScanner = true }) {
                        Text("Сканировать")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isQrVisible) {
                    val qrBitmap: Bitmap? = viewModel.generateWishlistQR()
                    if (qrBitmap != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Покажите этот код другу", color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp))
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Ваш QR Code",
                                modifier = Modifier.size(260.dp).background(Color.White).padding(8.dp)
                            )
                        }
                    } else {
                        Text("Добавьте хотя бы один фильм в вишлист,\nчтобы сгенерировать QR-код.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 32.dp))
                    }
                } else {
                    Text("Нажмите «Показать мой QR»,\nили отсканируйте код друга.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 64.dp))
                }

            } else {
                // ПОСЛЕ КОННЕКТА

                // Вкладки переключения
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    ) {
                        Text(
                            text = "Совпадения (${matchedItems.size})",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    ) {
                        Text(
                            text = "Все вместе (${combinedItems.size})",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Определяем, какой список сейчас показывать
                val currentList = if (selectedTabIndex == 0) matchedItems else combinedItems

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val titleText = if (selectedTabIndex == 0) {
                        if (matchedItems.isEmpty()) "Нет совпадений 😢" else "Вы оба хотите посмотреть:"
                    } else {
                        "Ваш общий список:"
                    }

                    Text(titleText, style = MaterialTheme.typography.titleMedium, color = Color.White)

                    // Кнопка-кубик выбирает рандомный фильм из ТЕКУЩЕЙ вкладки
                    IconButton(onClick = {
                        if (currentList.isNotEmpty()) {
                            randomSelectedMovie = currentList.random()
                            showRandomDialog = true
                        }
                    }) {
                        Text("🎲", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentList) { movie -> SharedMovieCard(movie = movie) }
                }
            }
        }
    }

    if (showRandomDialog && randomSelectedMovie != null) {
        RandomResultDialog(movie = randomSelectedMovie!!, onDismiss = { showRandomDialog = false })
    }
}

@Composable
fun SharedMovieCard(movie: Movie) {
    Card(modifier = Modifier.fillMaxWidth().height(100.dp), colors = CardDefaults.cardColors(containerColor = Color.DarkGray)) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path else "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = null,
                modifier = Modifier.width(70.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.Center) {
                Text(text = movie.title ?: "Без названия", color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                val ratingText = if (movie.vote_average == 0.0 || movie.vote_average == null) "-" else movie.vote_average.toString()
                Text(text = "⭐ $ratingText", color = Color.Yellow)
            }
        }
    }
}

@Composable
fun RandomResultDialog(movie: Movie, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "🎲 Случайный выбор") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = if (movie.poster_path?.startsWith("http") == true) movie.poster_path else "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                    contentDescription = null,
                    modifier = Modifier.height(200.dp).padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )
                Text(movie.title ?: "", style = MaterialTheme.typography.headlineSmall)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Ок") } }
    )
}