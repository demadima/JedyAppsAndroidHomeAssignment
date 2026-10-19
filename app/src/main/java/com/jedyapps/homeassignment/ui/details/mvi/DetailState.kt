package com.jedyapps.homeassignment.ui.details.mvi

import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.domain.model.MovieDetailObj

data class DetailState(
    val imdbId: String = "",
    val detailState: SResult<MovieDetailObj, Any?> = SResult.Initial
)
