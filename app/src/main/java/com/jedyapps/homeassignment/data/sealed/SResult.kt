package com.jedyapps.homeassignment.data.sealed

sealed interface SResult<out D, out E> {
    data object Initial : SResult<Nothing, Nothing>
    data object Loading : SResult<Nothing, Nothing>
    data class Success<out D, out E>(val data: D) : SResult<D, E>
    data class Error<out D, out E>(val error: E) : SResult<D, E>
}
