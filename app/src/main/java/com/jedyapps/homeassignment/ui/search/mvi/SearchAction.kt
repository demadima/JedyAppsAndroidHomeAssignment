package com.jedyapps.homeassignment.ui.search.mvi

import com.jedyapps.homeassignment.domain.model.MovieObj

sealed interface SearchAction {
    data class UpdateSearchText(val text: String) : SearchAction
    data class ExecuteSearch(val keyword: String) : SearchAction
    data object LoadMore : SearchAction
    data class ToggleFavorite(val movieObj: MovieObj) : SearchAction
    data class OnMovieClick(val imdbId: String) : SearchAction
    data object OnFavoritesClick : SearchAction
}
