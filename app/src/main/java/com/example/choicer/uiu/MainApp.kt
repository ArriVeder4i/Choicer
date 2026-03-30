package com.example.choicer.uiu

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.choicer.viewmodel.MovieViewModel
// ВАЖНО: Убедись, что нет красных импортов сверху. Если есть - удали их.

@Composable
fun MainApp(viewModel: MovieViewModel) {
    val navController = rememberNavController()

    val items = listOf(Screen.Home, Screen.Search, Screen.Wishlist, Screen.Friends)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // --- ГЛАВНЫЙ ЭКРАН ---
            composable(Screen.Home.route) {
                // Если HomeScreen горит красным, нажми Alt+Enter, чтобы импортировать
                // Или проверь, как точно называется функция в файле HomeScreen.kt
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { navController.navigate("details") }
                )
            }

            // --- ПОИСК ---
            composable(Screen.Search.route) {
                SearchScreen(viewModel = viewModel, onNavigateToDetails = { navController.navigate("details") })
            }

            // --- ДЕТАЛИ ---
            composable("details") {
                DetailsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
            }

            // --- ВИШЛИСТ ---
            composable(Screen.Wishlist.route) {
                WishlistScreen(viewModel = viewModel)
            }

            // --- ДРУЗЬЯ ---
            composable(Screen.Friends.route) {
                // Если тут горит красным, значит в файле FriendsScreen.kt функция
                // не принимает viewModel. Открой FriendsScreen.kt и убедись,
                // что там написано: fun FriendsScreen(viewModel: MovieViewModel)
                FriendsScreen(viewModel = viewModel)
            }
        }
    }
}