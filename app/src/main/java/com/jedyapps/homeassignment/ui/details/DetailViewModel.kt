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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route: DetailRoute = savedStateHandle.toRoute()
    val imdbId: String = route.imdbId

    private val _state = MutableStateFlow(DetailState(imdbId = imdbId))
    val state = _state.asStateFlow()

    private val _eventFlow = Channel<DetailEvent>(Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()

    private val detailRawState = MutableStateFlow<SResult<MovieDetailObj, Any?>>(SResult.Initial)

    init {
        observeDetailFlow(imdbId)
        fetchMovieDetail(imdbId)
    }

    fun onAction(action: DetailAction) {
        when (action) {
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

    private fun observeDetailFlow(imdbId: String) {
        viewModelScope.launch {
            combine(
                detailRawState,
                movieRepository.isFavoriteFlow(imdbId)
            ) { detailResult, isFav ->
                val mappedResult: SResult<MovieDetailObj, Any?> = when (detailResult) {
                    is SResult.Success -> {
                        SResult.Success(detailResult.data.copy(isFavorite = isFav))
                    }
                    else -> detailResult
                }
                mappedResult
            }.collectLatest { newResult ->
                _state.update { it.copy(detailState = newResult) }
            }
        }
    }

    private fun fetchMovieDetail(imdbId: String) {
        viewModelScope.launch {
            movieRepository.getMovieDetail(imdbId).collectLatest { result ->
                detailRawState.value = result
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
