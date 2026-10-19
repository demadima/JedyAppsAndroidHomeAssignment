package com.jedyapps.homeassignment.ui.favorites.mvi

import com.jedyapps.homeassignment.domain.model.MovieObj

data class FavoriteState(
    val favoriteMovieObjs: List<MovieObj> = emptyList()
)
