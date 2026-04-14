package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.*
import com.example.choicer.viewmodel.MovieViewModel

@Composable
fun MainApp(viewModel: MovieViewModel) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0),

        bottomBar = {
            if (currentRoute != "details" && !viewModel.isVideoMode.value) {

                NavigationBar(containerColor = Color.Black) {

                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Лента") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "search",
                        onClick = {
                            navController.navigate("search") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Search, null) },
                        label = { Text("Поиск") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF03A9F4),
                            selectedTextColor = Color(0xFF03A9F4)
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "wishlist",
                        onClick = {
                            navController.navigate("wishlist") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Favorite, null) },
                        label = { Text("Вишлист") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF4081),
                            selectedTextColor = Color(0xFFFF4081)
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "popular",
                        onClick = {
                            navController.navigate("popular") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Whatshot, null) },
                        label = { Text("Топ") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9800),
                            selectedTextColor = Color(0xFFFF9800)
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "friends",
                        onClick = {
                            navController.navigate("friends") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.People, null) },
                        label = { Text("Друзья") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4CAF50),
                            selectedTextColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .background(Color.Black)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            composable("home") {
                HomeScreen(viewModel) {
                    navController.navigate("details")
                }
            }

            composable("search") {
                SearchScreen(viewModel) {
                    navController.navigate("details")
                }
            }

            composable("popular") {
                PopularScreen(viewModel) {
                    navController.navigate("details")
                }
            }

            composable("friends") {
                FriendsScreen(viewModel) {
                    navController.navigate("details")
                }
            }

            composable("wishlist") {
                WishlistScreen(viewModel) {
                    navController.navigate("details")
                }
            }

            composable("details") {
                DetailsScreen(viewModel) {
                    navController.popBackStack()
                }
            }
        }
    }
}
