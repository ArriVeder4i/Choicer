package com.example.choicer.uiu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Главное", Icons.Default.Home)
    object Search : Screen("search", "Поиск", Icons.Default.Search)
    object Wishlist : Screen("wishlist", "Вишлист", Icons.Default.Favorite)
    object Friends : Screen("friends", "С друзьями", Icons.Default.Person)
}