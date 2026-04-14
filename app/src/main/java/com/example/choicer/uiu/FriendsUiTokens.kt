package com.example.choicer.uiu

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.choicer.data.Movie

internal val FriendsCardShape = RoundedCornerShape(32.dp)
internal val FriendsPillShape = RoundedCornerShape(28.dp)
internal val FriendsCardBorder = Color(0x33FFFFFF)
internal val FriendsGlass = Color(0x221B1730)
internal val FriendsGlassStrong = Color(0x44100D1D)
internal val FriendsPurpleStart = Color(0xFF6B44E8)
internal val FriendsPurpleEnd = Color(0xFFB04CFF)
internal val FriendsBlue = Color(0xFF5468FF)
internal val FriendsRedStart = Color(0xFF5F0A13)
internal val FriendsRedEnd = Color(0xFF8E1B28)
internal val FriendsYellow = Color(0xFFFFD348)
internal val FriendsGreen = Color(0xFF33D35E)
internal val FriendsMutedText = Color(0xCCFFFFFF)

internal data class FriendsSectionData(
    val title: String,
    val icon: String,
    val movies: List<Movie>,
    val tabIndex: Int
)
