package com.example.choicer.data

// Модель для одного клипа
data class MovieClip(
    val driveId: String, // Идентификатор файла на Google Диске
    val previewUrl: String? = null
)

// Контейнер для связи фильма и его клипов
data class MovieClipsContainer(
    val movieId: Int,
    val clips: List<MovieClip>
)

// Структура JSON ответа
data class RemoteClipResponse(
    val movies: List<MovieClipsContainer>
)