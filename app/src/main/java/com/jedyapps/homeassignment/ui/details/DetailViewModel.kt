package com.jedyapps.homeassignment.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jedyapps.homeassignment.data.repository.MovieRepository
import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.domain.model.MovieDetailObj
import com.jedyapps.homeassignment.ui.details.mvi.DetailAction
import com.jedyapps.homeassignment.ui.details.mvi.DetailEvent
import com.jedyapps.homeassignment.ui.details.mvi.DetailState
import com.jedyapps.homeassignment.ui.navigation.DetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val detailRoute: DetailRoute = savedStateHandle.toRoute()

    private val _state = MutableStateFlow(DetailState(imdbId = detailRoute.imdbId))
    val state = _state.asStateFlow()

    private val _eventFlow = Channel<DetailEvent>(Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()

    init {
        loadDetail(detailRoute.imdbId)
    }

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.LoadDetail -> {
                _state.update { it.copy(imdbId = action.imdbId) }
                loadDetail(action.imdbId)
            }
            is DetailAction.ToggleFavorite -> {
                toggleFavorite(action.detail)
            }
            is DetailAction.OnBackClick -> {
                viewModelScope.launch {
                    _eventFlow.send(DetailEvent.NavigateBack)
                }
            }
        }
    }

    private fun loadDetail(imdbId: String) {
        viewModelScope.launch {
            movieRepository.getMovieDetail(imdbId).collectLatest { result ->
                _state.update { it.copy(detailState = result) }
            }
        }

        // Observe real-time favorite updates
        viewModelScope.launch {
            movieRepository.isFavoriteFlow(imdbId).collectLatest { isFav ->
                val current = _state.value.detailState
                if (current is SResult.Success) {
                    _state.update { currentState ->
                        val currentResult = currentState.detailState
                        if (currentResult is SResult.Success) {
                            currentState.copy(
                                detailState = SResult.Success(currentResult.data.copy(isFavorite = isFav))
                            )
                        } else {
                            currentState
                        }
                    }
                }
            }
        }
    }

    private fun toggleFavorite(detail: MovieDetailObj) {
        viewModelScope.launch {
            if (detail.isFavorite) {
                movieRepository.removeFavorite(detail.imdbID)
            } else {
                movieRepository.addFavoriteDetail(detail)
            }
        }
    }
}
