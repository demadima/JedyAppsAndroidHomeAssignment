package com.jedyapps.homeassignment.ui.details.mvi

import com.jedyapps.homeassignment.domain.model.MovieDetailObj

sealed interface DetailAction {
    data class LoadDetail(val imdbId: String) : DetailAction
    data class ToggleFavorite(val detail: MovieDetailObj) : DetailAction
    data object OnBackClick : DetailAction
}
