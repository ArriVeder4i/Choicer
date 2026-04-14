package com.example.choicer.data

import android.util.Base64

private const val KinopoiskPosterPrefix = "K"
private const val DirectPosterPrefix = "T"
private const val MaxSharedMovies = 20

object WishlistShareCodec {
    fun encode(movies: List<Movie>): String {
        val payload = movies
            .take(MaxSharedMovies)
            .joinToString(";") { movie ->
                val safeTitle = movie.title
                    .replace(";", "")
                    .replace("|", "")

                listOf(
                    movie.id.toString(),
                    safeTitle,
                    movie.sharePosterToken(),
                    movie.vote_average ?: 0.0
                ).joinToString("|")
            }

        if (payload.isBlank()) return ""

        return Base64.encodeToString(
            payload.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
    }

    fun decode(encodedPayload: String): List<Movie> {
        if (encodedPayload.isBlank()) return emptyList()

        val decodedPayload = String(
            Base64.decode(encodedPayload, Base64.NO_WRAP or Base64.URL_SAFE),
            Charsets.UTF_8
        )

        return decodedPayload
            .split(";")
            .mapNotNull(::decodeMovie)
    }

    private fun decodeMovie(rawMovie: String): Movie? {
        val parts = rawMovie.split("|")
        if (parts.size < 4) return null

        val movieId = parts[0].toIntOrNull() ?: return null

        return Movie(
            id = movieId,
            title = parts[1],
            poster_path = decodePosterPath(movieId, parts[2]),
            overview = "",
            release_date = "",
            vote_average = parts[3].toDoubleOrNull() ?: 0.0
        )
    }

    private fun Movie.sharePosterToken(): String =
        if (poster_path?.contains("kp/") == true) {
            "$KinopoiskPosterPrefix$id"
        } else {
            "$DirectPosterPrefix${poster_path.orEmpty()}"
        }

    private fun decodePosterPath(movieId: Int, rawPosterToken: String): String =
        when {
            rawPosterToken.startsWith(KinopoiskPosterPrefix) ->
                "https://kinopoiskapiunofficial.tech/images/posters/kp/$movieId.jpg"

            rawPosterToken.startsWith(DirectPosterPrefix) ->
                rawPosterToken.removePrefix(DirectPosterPrefix)

            else -> rawPosterToken
        }
}
