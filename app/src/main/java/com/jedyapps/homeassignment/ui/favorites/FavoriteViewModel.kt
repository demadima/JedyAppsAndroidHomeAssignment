package com.jedyapps.homeassignment.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jedyapps.homeassignment.data.repository.MovieRepository
import com.jedyapps.homeassignment.ui.favorites.mvi.FavoriteAction
import com.jedyapps.homeassignment.ui.favorites.mvi.FavoriteEvent
import com.jedyapps.homeassignment.ui.favorites.mvi.FavoriteState
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
class FavoriteViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoriteState())
    val state = _state.asStateFlow()

    private val _eventFlow = Channel<FavoriteEvent>(Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()

    init {
        viewModelScope.launch {
            movieRepository.getFavoriteMovies().collectLatest { favorites ->
                _state.update { it.copy(favoriteMovieObjs = favorites) }
            }
        }
    }

    fun onAction(action: FavoriteAction) {
        when (action) {
            is FavoriteAction.ToggleFavorite -> {
                viewModelScope.launch {
                    movieRepository.toggleFavorite(action.movieObj)
                }
            }
            is FavoriteAction.OnMovieClick -> {
                viewModelScope.launch {
                    _eventFlow.send(FavoriteEvent.NavigateToDetail(action.imdbId))
                }
            }
            is FavoriteAction.OnBackClick -> {
                viewModelScope.launch {
                    _eventFlow.send(FavoriteEvent.NavigateBack)
                }
            }
        }
    }
}
