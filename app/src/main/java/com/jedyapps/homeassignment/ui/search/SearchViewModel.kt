package com.jedyapps.homeassignment.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jedyapps.homeassignment.data.repository.MovieRepository
import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.domain.model.MovieObj
import com.jedyapps.homeassignment.domain.model.pagination.PaginationState
import com.jedyapps.homeassignment.ui.search.model.SearchListItem
import com.jedyapps.homeassignment.ui.search.model.toSearchListItems
import com.jedyapps.homeassignment.ui.search.mvi.SearchAction
import com.jedyapps.homeassignment.ui.search.mvi.SearchEvent
import com.jedyapps.homeassignment.ui.search.mvi.SearchState
import com.jedyapps.sdk.ads.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val adManager: AdManager
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private val _eventFlow = Channel<SearchEvent>(Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()

    private var searchJob: Job? = null

    // Raw search result flow holding MovieObj items, combined reactively with favorites and native ads
    private val moviesRawState = MutableStateFlow<SResult<PaginationState<List<MovieObj>>, Any?>>(SResult.Initial)
    private val favoriteIdsFlow = movieRepository.getFavoriteMovies().map { list -> list.map { it.imdbID }.toSet() }

    init {
        observeSearchFlow()
    }

    private fun observeSearchFlow() {
        viewModelScope.launch {
            combine(
                moviesRawState,
                favoriteIdsFlow,
                adManager.nativeAd1,
                adManager.nativeAd2
            ) { searchResult, favoritesSet, ad1, ad2 ->
                _state.update { it.copy(nativeAd1 = ad1, nativeAd2 = ad2) }

                val mappedResult: SResult<PaginationState<List<SearchListItem>>, Any?> = when (searchResult) {
                    is SResult.Success -> {
                        val pagination = searchResult.data
                        val updatedMovies = pagination.items.map { movie ->
                            movie.copy(isFavorite = favoritesSet.contains(movie.imdbID))
                        }
                        val listItems = updatedMovies.toSearchListItems(ad1, ad2)
                        SResult.Success(
                            PaginationState(
                                items = listItems,
                                currentPage = pagination.currentPage,
                                isLoading = pagination.isLoading,
                                endReached = pagination.endReached
                            )
                        )
                    }
                    is SResult.Error -> SResult.Error(searchResult.error)
                    is SResult.Loading -> SResult.Loading
                    is SResult.Initial -> SResult.Initial
                }
                mappedResult
            }.collectLatest { newResult ->
                _state.update { it.copy(searchResultState = newResult) }
            }
        }
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.UpdateSearchText -> {
                _state.update { it.copy(searchText = action.text) }
                searchJob?.cancel()
                if (action.text.isBlank()) {
                    moviesRawState.value = SResult.Initial
                } else {
                    searchJob = viewModelScope.launch {
                        delay(800.milliseconds) // Debounce search
                        executeSearch(action.text, page = 1)
                    }
                }
            }
            is SearchAction.ExecuteSearch -> {
                searchJob?.cancel()
                if (action.keyword.isBlank()) {
                    moviesRawState.value = SResult.Initial
                } else {
                    executeSearch(action.keyword, page = 1)
                }
            }
            is SearchAction.LoadMore -> {
                val currentPagination = (moviesRawState.value as? SResult.Success)?.data
                if (currentPagination != null && !currentPagination.isLoading && !currentPagination.endReached) {
                    executeSearch(_state.value.searchText, page = currentPagination.currentPage + 1)
                }
            }
            is SearchAction.ToggleFavorite -> {
                viewModelScope.launch {
                    movieRepository.toggleFavorite(action.movieObj)
                }
            }
            is SearchAction.OnMovieClick -> {
                viewModelScope.launch {
                    _eventFlow.send(SearchEvent.NavigateToDetail(action.imdbId))
                }
            }
            is SearchAction.OnFavoritesClick -> {
                viewModelScope.launch {
                    _eventFlow.send(SearchEvent.NavigateToFavorites)
                }
            }
        }
    }

    private fun executeSearch(keyword: String, page: Int = 1) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) {
            moviesRawState.value = SResult.Initial
            return
        }

        val currentPagination = (moviesRawState.value as? SResult.Success)?.data
        if (page > 1 && (currentPagination?.isLoading == true || currentPagination?.endReached == true)) return

        if (page == 1) {
            moviesRawState.value = SResult.Loading
        }

        viewModelScope.launch {
            movieRepository.searchMovies(trimmed, page)
                .collectLatest { result ->

                when (result) {
                    is SResult.Loading -> {
                        if (page == 1) {
                            moviesRawState.value = SResult.Loading
                        } else {
                            val activePagination = (moviesRawState.value as? SResult.Success)?.data
                            activePagination?.let {
                                moviesRawState.value = SResult.Success(it.copy(isLoading = true))
                            }
                        }
                    }
                    is SResult.Success -> {
                        val currentData = (moviesRawState.value as? SResult.Success)?.data
                        val existingItems = if (page == 1) emptyList() else (currentData?.items ?: emptyList())
                        val combinedList = existingItems + result.data.items

                        moviesRawState.value = SResult.Success(
                            PaginationState(
                                items = combinedList,
                                currentPage = result.data.currentPage,
                                isLoading = false,
                                endReached = result.data.endReached
                            )
                        )
                    }
                    is SResult.Error -> {
                        moviesRawState.value = SResult.Error(result.error)
                    }
                    else -> {}
                }
            }
        }
    }
}
