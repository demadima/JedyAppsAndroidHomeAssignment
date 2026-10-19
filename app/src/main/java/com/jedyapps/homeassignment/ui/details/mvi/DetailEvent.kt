package com.jedyapps.homeassignment.ui.details.mvi

import com.jedyapps.homeassignment.data.sealed.UIText

sealed class DetailEvent {
    data class ShowToast(val message: UIText) : DetailEvent()
    data object NavigateBack : DetailEvent()
}
