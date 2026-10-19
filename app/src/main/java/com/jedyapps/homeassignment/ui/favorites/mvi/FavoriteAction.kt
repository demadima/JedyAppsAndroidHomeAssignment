package com.jedyapps.homeassignment.ui.favorites.mvi

import com.jedyapps.homeassignment.domain.model.MovieObj

sealed interface FavoriteAction {
    data class ToggleFavorite(val movieObj: MovieObj) : FavoriteAction
    data class OnMovieClick(val imdbId: String) : FavoriteAction
    data object OnBackClick : FavoriteAction
}
