package com.jedyapps.homeassignment.ui.details.mvi

import com.jedyapps.homeassignment.domain.model.MovieDetailObj

sealed interface DetailAction {
    data class ToggleFavorite(val detail: MovieDetailObj) : DetailAction
    data object OnBackClick : DetailAction
}
