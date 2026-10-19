package com.jedyapps.homeassignment.domain.model

import com.jedyapps.homeassignment.data.local.entity.FavoriteMovieEntity
import com.jedyapps.homeassignment.data.model.dto.OmdbMovieDto

data class MovieObj(
    val imdbID: String,
    val title: String,
    val year: String,
    val type: String,
    val poster: String,
    val isFavorite: Boolean = false
)

fun OmdbMovieDto.toDomain(isFavorite: Boolean = false): MovieObj {
    return MovieObj(
        imdbID = imdbID,
        title = title ?: "Unknown Title",
        year = year ?: "",
        type = type ?: "movie",
        poster = poster ?: "",
        isFavorite = isFavorite
    )
}

fun FavoriteMovieEntity.toDomain(): MovieObj {
    return MovieObj(
        imdbID = imdbID,
        title = title,
        year = year,
        type = type,
        poster = poster,
        isFavorite = true
    )
}

fun MovieObj.toEntity(): FavoriteMovieEntity {
    return FavoriteMovieEntity(
        imdbID = imdbID,
        title = title,
        year = year,
        type = type,
        poster = poster
    )
}
