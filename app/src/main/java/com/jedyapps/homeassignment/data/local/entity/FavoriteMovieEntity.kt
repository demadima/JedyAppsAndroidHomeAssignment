package com.jedyapps.homeassignment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val imdbID: String,
    val title: String,
    val year: String,
    val type: String,
    val poster: String,
    val plot: String = "",
    val genre: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
