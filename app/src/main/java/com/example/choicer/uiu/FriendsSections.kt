package com.example.choicer.uiu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie

@Composable
internal fun FriendsHeader() {
    AppScreenTitle(
        text = "Друзья и Совпадения\u00A0🤝",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        maxLines = 1
    )
}

@Composable
internal fun FriendsSegmentedTabs(
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FriendsPillShape)
            .background(Color(0x271A1530))
            .border(1.dp, FriendsCardBorder, FriendsPillShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FriendsTabChip(
            text = "Совпадения",
            selected = selectedTabIndex == 0,
            modifier = Modifier.weight(1f),
            onClick = { onSelectTab(0) }
        )
        FriendsTabChip(
            text = "Все вместе",
            selected = selectedTabIndex == 1,
            modifier = Modifier.weight(1f),
            onClick = { onSelectTab(1) }
        )
    }
}

@Composable
internal fun FriendsTabChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundModifier = if (selected) {
        Modifier.background(
            Brush.horizontalGradient(listOf(FriendsPurpleStart, FriendsPurpleEnd)),
            FriendsPillShape
        )
    } else {
        Modifier.background(Color.Transparent, FriendsPillShape)
    }

    Box(
        modifier = modifier
            .clip(FriendsPillShape)
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.82f),
            fontSize = 17.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
internal fun FriendsMovieSection(
    section: FriendsSectionData,
    highlighted: Boolean,
    onMovieClick: (Movie) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FriendsSectionHeader(
            title = section.title,
            icon = section.icon,
            actionText = null,
            onActionClick = null
        )
        if (section.movies.isEmpty()) {
            EmptyMoviesCard(
                highlighted = highlighted,
                text = if (section.tabIndex == 0) {
                    "Совпадений пока нет. Попробуйте отсканировать QR друга."
                } else {
                    "Общая лента появится после подключения."
                }
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(section.movies) { movie ->
                    FriendsMoviePosterCard(
                        movie = movie,
                        highlighted = highlighted,
                        onClick = { onMovieClick(movie) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun FriendsSectionHeader(
    title: String,
    icon: String,
    actionText: String?,
    onActionClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$icon $title",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = "$actionText ›",
                color = Color(0xFFB266FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
internal fun FriendsMoviePosterCard(
    movie: Movie,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .width(152.dp)
            .height(246.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF161222), Color(0xFF0A0910)))
            )
            .border(
                width = if (highlighted) 1.3.dp else 1.dp,
                color = if (highlighted) Color(0x665F4DFF) else FriendsCardBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
    ) {
        if (movie.poster_path?.isNotBlank() == true) {
            AsyncImage(
                model = movie.posterUrl(),
                contentDescription = movie.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color(0xAA06050B),
                        Color(0xF506050B)
                    )
                )
            )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "⭐ ${movie.formattedRating}",
                color = FriendsYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
internal fun EmptyMoviesCard(highlighted: Boolean, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (highlighted) FriendsGlassStrong else FriendsGlass)
            .border(1.dp, FriendsCardBorder, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Пока пусто",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = text,
            color = FriendsMutedText,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Start
        )
    }
}
