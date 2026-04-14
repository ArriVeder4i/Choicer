package com.example.choicer.uiu

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.choicer.data.Movie

@Composable
internal fun MatchHeroCard(
    percent: Int,
    rating: Double,
    rolling: Boolean,
    heroMovie: Movie?,
    onToggleQr: () -> Unit,
    onDisconnect: () -> Unit,
    onRandomMovie: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 610.dp)
            .clip(FriendsCardShape)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF120A24), Color(0xFF090612)))
            )
            .border(1.dp, FriendsCardBorder, FriendsCardShape)
    ) {
        if (heroMovie != null) {
            AsyncImage(
                model = heroMovie.posterUrl(),
                contentDescription = heroMovie.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xB00A0714), Color(0x500B0916), Color(0xF006040F))
                )
            )
        )

        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.radialGradient(
                    colors = listOf(Color(0x55E14D86), Color.Transparent)
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .matchParentSize()
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MatchPercentChip(percent = percent)

                Text(
                    text = buildAnnotatedString {
                        append("🔥 Рейтинг:")
                        withStyle(
                            SpanStyle(color = FriendsYellow, fontWeight = FontWeight.SemiBold)
                        ) {
                            append(" ${String.format("%.1f", rating)}")
                        }
                    },
                    color = Color.White,
                    fontSize = 18.sp
                )

                Text(
                    text = heroMovie?.title ?: "Совместный вечер уже почти собран",
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 250.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FriendsActionButton(
                        text = "QR",
                        leadingBadge = "▦",
                        brush = Brush.linearGradient(listOf(Color(0x403B3B4A), Color(0x2213131A))),
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        compact = true,
                        textMaxLines = 1,
                        textSize = 16.sp,
                        onClick = onToggleQr
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    FriendsActionButton(
                        text = "Отключить",
                        leadingBadge = "⊘",
                        brush = Brush.horizontalGradient(listOf(FriendsRedStart, FriendsRedEnd)),
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        compact = true,
                        textMaxLines = 1,
                        textSize = 16.sp,
                        onClick = onDisconnect
                    )
                }

                FriendsActionButton(
                    text = if (rolling) "КРУТИМ..." else "СЛУЧАЙНЫЙ ФИЛЬМ",
                    leadingBadge = "🎲",
                    brush = Brush.horizontalGradient(listOf(FriendsPurpleStart, FriendsBlue)),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRandomMovie
                )
            }
        }
    }
}

@Composable
internal fun DiscoverHeroCard(
    isQrVisible: Boolean,
    onToggleQr: () -> Unit,
    onScan: () -> Unit
) {
    val shape = RoundedCornerShape(30.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF101427),
                        Color(0xFF171634),
                        Color(0xFF0B0B18)
                    )
                )
            )
            .border(1.dp, Color(0x4C8F79FF), shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x5E5E73FF), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(120f, 110f),
                        radius = 460f
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x6EB73DFF), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(540f, 120f),
                        radius = 420f
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x24FFFFFF), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(410f, 96f),
                        radius = 90f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xFF7B75FF),
                            Color(0xFFE96FFF),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(10.dp)
                .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(24.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isQrVisible) "Покажите QR\nдругу рядом" else "Найдите друга\nдля совпадений",
                    color = Color.White,
                    fontSize = 28.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (isQrVisible) "Ваш код уже готов к обмену" else "Мгновенное подключение через QR",
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiscoverActionButton(
                    text = if (isQrVisible) "Скрыть QR" else "Мой QR",
                    leading = "QR",
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF825DFF),
                            Color(0xFFE56CFF)
                        )
                    ),
                    modifier = Modifier.weight(1f),
                    glowColor = Color(0x338B6DFF),
                    onClick = onToggleQr
                )
                DiscoverActionButton(
                    text = "Скан",
                    leading = "◎",
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF30354E),
                            Color(0xFF1D2030)
                        )
                    ),
                    modifier = Modifier.weight(1f),
                    glowColor = Color(0x2237B6FF),
                    onClick = onScan
                )
            }
        }
    }
}

@Composable
internal fun DiscoverActionButton(
    text: String,
    leading: String,
    brush: Brush,
    modifier: Modifier = Modifier,
    glowColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .height(82.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 4.dp, vertical = 5.dp)
                .clip(shape)
                .background(glowColor)
        )
        Row(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(brush)
                .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = leading,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = if (leading.length > 1) 9.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun MatchPercentChip(percent: Int) {
    Box(
        modifier = Modifier
            .clip(FriendsPillShape)
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFE04D82), Color(0xFF5768FF)))
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            text = "❤️ Совпадение: $percent%",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun FriendsActionButton(
    text: String,
    brush: Brush,
    modifier: Modifier = Modifier,
    leadingBadge: String? = null,
    compact: Boolean = false,
    textMaxLines: Int = if (compact) 2 else 1,
    textSize: TextUnit = if (compact) 16.sp else 18.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(FriendsPillShape)
            .background(brush)
            .border(1.dp, FriendsCardBorder, FriendsPillShape)
            .clickable(onClick = onClick)
            .heightIn(min = if (compact) 62.dp else 0.dp)
            .padding(
                horizontal = if (compact) 10.dp else 18.dp,
                vertical = if (compact) 10.dp else 18.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        if (leadingBadge != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = if (leadingBadge.length > 1) 6.dp else 7.dp,
                            vertical = 4.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = leadingBadge,
                        color = Color.White,
                        fontSize = if (leadingBadge.length > 1) 10.sp else 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = textSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = textMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = if (compact) textSize.value.sp else 20.sp
                )
            }
        } else {
            Text(
                text = text,
                color = Color.White,
                fontSize = textSize,
                fontWeight = FontWeight.Medium,
                maxLines = textMaxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) textSize.value.sp else 20.sp
            )
        }
    }
}

@Composable
internal fun QrPreviewCard(bitmap: Bitmap, title: String, subtitle: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(FriendsGlassStrong)
            .border(1.dp, FriendsCardBorder, RoundedCornerShape(26.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = FriendsMutedText,
                fontSize = 13.sp
            )
        }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(10.dp)
        )
    }
}
