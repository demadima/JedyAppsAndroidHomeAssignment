package com.jedyapps.homeassignment.ui.favorites.mvi

sealed class FavoriteEvent {
    data class NavigateToDetail(val imdbId: String) : FavoriteEvent()
    data object NavigateBack : FavoriteEvent()
}
