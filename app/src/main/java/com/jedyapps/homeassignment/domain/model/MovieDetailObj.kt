package com.jedyapps.homeassignment.domain.model

import com.jedyapps.homeassignment.data.local.entity.FavoriteMovieEntity
import com.jedyapps.homeassignment.data.model.dto.OmdbDetailResponse

data class MovieDetailObj(
    val imdbID: String,
    val title: String,
    val year: String,
    val type: String,
    val poster: String,
    val plot: String,
    val genre: String,
    val director: String,
    val actors: String,
    val imdbRating: String,
    val released: String,
    val isFavorite: Boolean = false
)

fun OmdbDetailResponse.toDomain(isFavorite: Boolean = false): MovieDetailObj {
    return MovieDetailObj(
        imdbID = imdbID ?: "",
        title = title ?: "Unknown",
        year = year ?: "",
        type = type ?: "movie",
        poster = poster ?: "",
        plot = plot ?: "No plot available.",
        genre = genre ?: "N/A",
        director = director ?: "N/A",
        actors = actors ?: "N/A",
        imdbRating = imdbRating ?: "N/A",
        released = released ?: "N/A",
        isFavorite = isFavorite
    )
}

fun MovieDetailObj.toEntity(): FavoriteMovieEntity {
    return FavoriteMovieEntity(
        imdbID = imdbID,
        title = title,
        year = year,
        type = type,
        poster = poster,
        plot = plot,
        genre = genre
    )
}
