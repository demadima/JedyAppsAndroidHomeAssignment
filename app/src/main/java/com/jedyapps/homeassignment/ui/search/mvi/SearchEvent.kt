package com.jedyapps.homeassignment.ui.search.mvi

import com.jedyapps.homeassignment.data.sealed.UIText

sealed class SearchEvent {
    data class ShowToast(val message: UIText) : SearchEvent()
    data class NavigateToDetail(val imdbId: String) : SearchEvent()
    data object NavigateToFavorites : SearchEvent()
}
