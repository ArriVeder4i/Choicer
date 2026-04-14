package com.example.choicer.uiu

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.choicer.data.BleManager
import com.example.choicer.data.Movie
import com.example.choicer.viewmodel.MovieViewModel
import com.example.choicer.viewmodel.ScannedDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(viewModel: MovieViewModel, onNavigateToDetails: () -> Unit) {
    val isConnected by viewModel.isConnected.collectAsState()
    val matchedItems by viewModel.matchedMovies.collectAsState()
    val combinedItems by viewModel.combinedMovies.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val percent by viewModel.compatibilityPercent.collectAsState()
    val rating by viewModel.compatibilityRating.collectAsState()

    var isQrVisible by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var rolling by remember { mutableStateOf(false) }
    var showMatchAnimation by remember { mutableStateOf(false) }
    var isDiscoveryActive by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasPermissions = permissions.values.any { it }
        isDiscoveryActive = hasPermissions
        if (hasPermissions) {
            viewModel.clearDiscoveredDevices()
            BleManager.start(context)
        }
    }

    LaunchedEffect(Unit) {
        BleManager.getData = { viewModel.generateWishlistQRData() }
        BleManager.onReceive = { data -> viewModel.matchWishlists(data) }
        BleManager.onDeviceFound = { device, deviceName ->
            viewModel.addDiscoveredDevice(device, deviceName)
        }
    }

    DisposableEffect(Unit) {
        onDispose { BleManager.stop() }
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            showMatchAnimation = true
            delay(1500)
            showMatchAnimation = false
        }
    }

    if (showScanner) {
        FriendsScannerOverlay(
            onClose = { showScanner = false },
            onCodeScanned = { scannedData ->
                showScanner = false
                viewModel.matchWishlists(scannedData)
            }
        )
        return
    }

    val primarySection = if (selectedTabIndex == 0) {
        FriendsSectionData("Ваши совпадения", "✨", matchedItems, 0)
    } else {
        FriendsSectionData("Все вместе", "👥", combinedItems, 1)
    }
    val secondarySection = if (selectedTabIndex == 0) {
        FriendsSectionData("Все вместе", "👥", combinedItems, 1)
    } else {
        FriendsSectionData("Ваши совпадения", "✨", matchedItems, 0)
    }
    val heroMovie = primarySection.movies.firstOrNull() ?: secondarySection.movies.firstOrNull()
    val qrBitmap = if (isQrVisible) viewModel.generateWishlistQR() else null
    val stopDiscovery = {
        BleManager.stop()
        isDiscoveryActive = false
    }

    BluredGradientBackground(noPadding = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isConnected) {
                ConnectedFriendsContent(
                    percent = percent,
                    rating = rating,
                    selectedTabIndex = selectedTabIndex,
                    isQrVisible = isQrVisible,
                    qrBitmap = qrBitmap,
                    rolling = rolling,
                    heroMovie = heroMovie,
                    primarySection = primarySection,
                    secondarySection = secondarySection,
                    onToggleQr = { isQrVisible = !isQrVisible },
                    onDisconnect = {
                        isQrVisible = false
                        isDiscoveryActive = false
                        BleManager.stop()
                        viewModel.disconnect()
                    },
                    onRandomMovie = {
                        rolling = true
                        val movie = if (selectedTabIndex == 0) {
                            viewModel.getRandomMovieFromMatch()
                        } else {
                            viewModel.getRandomMovieFromCombined()
                        }
                        scope.launch {
                            delay(800)
                            rolling = false
                            movie?.let {
                                viewModel.selectedMovieForDetails.value = it
                                viewModel.loadExtraDetails(it.id)
                                onNavigateToDetails()
                            }
                        }
                    },
                    onSelectTab = { selectedTabIndex = it },
                    onMovieClick = { movie ->
                        viewModel.selectedMovieForDetails.value = movie
                        viewModel.loadExtraDetails(movie.id)
                        onNavigateToDetails()
                    }
                )
            } else {
                DisconnectedFriendsContent(
                    isQrVisible = isQrVisible,
                    qrBitmap = qrBitmap,
                    isDiscoveryActive = isDiscoveryActive,
                    discoveredDevices = discoveredDevices,
                    onToggleQr = { isQrVisible = !isQrVisible },
                    onScan = { showScanner = true },
                    onStopDiscovery = stopDiscovery,
                    onStartDiscovery = {
                        BleManager.stop()
                        isDiscoveryActive = false
                        viewModel.clearDiscoveredDevices()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_ADVERTISE,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                            )
                        } else {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    onDeviceClick = { device -> viewModel.connectToFoundDevice(device) }
                )
            }

            FriendsMatchOverlay(
                visible = showMatchAnimation,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun FriendsScannerOverlay(
    onClose: () -> Unit,
    onCodeScanned: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        QrScannerView(onCodeScanned = onCodeScanned)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

@Composable
private fun FriendsMatchOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF6727B3), Color(0xFFE14D86))
                    )
                )
                .border(1.dp, FriendsCardBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 30.dp, vertical = 22.dp)
        ) {
            Text(
                text = "❤️ Совпадение найдено",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ConnectedFriendsContent(
    percent: Int,
    rating: Double,
    selectedTabIndex: Int,
    isQrVisible: Boolean,
    qrBitmap: Bitmap?,
    rolling: Boolean,
    heroMovie: Movie?,
    primarySection: FriendsSectionData,
    secondarySection: FriendsSectionData,
    onToggleQr: () -> Unit,
    onDisconnect: () -> Unit,
    onRandomMovie: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { FriendsHeader() }
        item {
            MatchHeroCard(
                percent = percent,
                rating = rating,
                rolling = rolling,
                heroMovie = heroMovie,
                onToggleQr = onToggleQr,
                onDisconnect = onDisconnect,
                onRandomMovie = onRandomMovie
            )
        }
        if (isQrVisible && qrBitmap != null) {
            item {
                QrPreviewCard(
                    bitmap = qrBitmap,
                    title = "Ваш QR-код",
                    subtitle = null
                )
            }
        }
        item {
            FriendsSegmentedTabs(
                selectedTabIndex = selectedTabIndex,
                onSelectTab = onSelectTab
            )
        }
        item {
            FriendsMovieSection(
                section = primarySection,
                highlighted = true,
                onMovieClick = onMovieClick
            )
        }
        item {
            FriendsMovieSection(
                section = secondarySection,
                highlighted = false,
                onMovieClick = onMovieClick
            )
        }
    }
}

@Composable
private fun DisconnectedFriendsContent(
    isQrVisible: Boolean,
    qrBitmap: Bitmap?,
    isDiscoveryActive: Boolean,
    discoveredDevices: List<ScannedDevice>,
    onToggleQr: () -> Unit,
    onScan: () -> Unit,
    onStopDiscovery: () -> Unit,
    onStartDiscovery: () -> Unit,
    onDeviceClick: (ScannedDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { FriendsHeader() }
        item {
            DiscoverHeroCard(
                isQrVisible = isQrVisible,
                onToggleQr = onToggleQr,
                onScan = onScan
            )
        }
        if (isQrVisible && qrBitmap != null) {
            item {
                QrPreviewCard(
                    bitmap = qrBitmap,
                    title = "Ваш QR-код",
                    subtitle = null
                )
            }
        }
        item {
            FriendsSectionHeader(
                title = "Устройства поблизости",
                icon = "\uD83D\uDCF1",
                actionText = null,
                onActionClick = null
            )
        }
        item {
            if (isDiscoveryActive) {
                SearchingDevicesCard(
                    hasDiscoveredDevices = discoveredDevices.isNotEmpty(),
                    onClick = onStopDiscovery
                )
            } else {
                StartDiscoveryCard(onClick = onStartDiscovery)
            }
        }
        if (discoveredDevices.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    discoveredDevices.forEach { device ->
                        NearbyDeviceCard(device = device, onClick = { onDeviceClick(device) })
                    }
                }
            }
        }
    }
}
