package com.jedyapps.homeassignment.ui.search

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jedyapps.homeassignment.R
import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.data.sealed.asUIText
import com.jedyapps.homeassignment.domain.model.MovieObj
import com.jedyapps.homeassignment.domain.model.pagination.PaginationState
import com.jedyapps.homeassignment.ui.components.MovieCardUI
import com.jedyapps.homeassignment.ui.components.TopBarUI
import com.jedyapps.homeassignment.ui.search.model.SearchListItem
import com.jedyapps.homeassignment.ui.search.model.toSearchListItems
import com.jedyapps.homeassignment.ui.search.mvi.SearchAction
import com.jedyapps.homeassignment.ui.search.mvi.SearchEvent
import com.jedyapps.homeassignment.ui.search.mvi.SearchState
import com.jedyapps.homeassignment.ui.theme.JedyAppsAndroidHomeAssignmentTheme
import com.jedyapps.sdk.wrapers.NativeAdWrapper
import com.jedyapps.sdk.ui.NativeAdItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Trigger initial search if keyword is non-empty
    LaunchedEffect(Unit) {
        if (state.searchText.isNotEmpty() && state.searchResultState is SResult.Initial) {
            viewModel.onAction(SearchAction.ExecuteSearch(state.searchText))
        }
    }

    // Handle single-shot MVI events
    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SearchEvent.ShowToast -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                }
                is SearchEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.imdbId)
                }
                is SearchEvent.NavigateToFavorites -> {
                    onNavigateToFavorites()
                }
            }
        }
    }

    SearchScreenContent(
        state = state,
        onAction = { viewModel.onAction(it) }
    )
}

@Composable
fun SearchScreenContent(
    state: SearchState,
    onAction: (SearchAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopBarUI(
                title = stringResource(R.string.search_screen_title),
                onFavoritesClick = { onAction(SearchAction.OnFavoritesClick) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = state.searchText,
                onValueChange = { newText ->
                    onAction(SearchAction.UpdateSearchText(newText))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (state.searchText.isNotEmpty()) {
                        IconButton(onClick = { onAction(SearchAction.UpdateSearchText("")) }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search Content / List
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (val result = state.searchResultState) {
                    is SResult.Loading -> {
                        CircularProgressIndicator()
                    }
                    is SResult.Error -> {
                        Text(
                            text = result.error.asUIText().asString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    is SResult.Initial -> {
                        Text(
                            text = stringResource(R.string.search_initial_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is SResult.Success -> {
                        val pagination = result.data
                        val items = pagination.items
                        if (items.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_movies_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(
                                    items = items,
                                    key = { _, item ->
                                        when (item) {
                                            is SearchListItem.MovieItem -> item.movieObj.imdbID
                                            is SearchListItem.NativeAdItem -> item.id
                                        }
                                    }
                                ) { index, item ->
                                    // Trigger loadMore when scrolling near the end
                                    if (index >= items.size - 2 &&
                                        !pagination.isLoading &&
                                        !pagination.endReached
                                    ) {
                                        onAction(SearchAction.LoadMore)
                                    }

                                    when (item) {
                                        is SearchListItem.MovieItem -> {
                                            MovieCardUI(
                                                movieObj = item.movieObj,
                                                onClick = {
                                                    onAction(SearchAction.OnMovieClick(item.movieObj.imdbID))
                                                },
                                                onFavoriteToggle = {
                                                    onAction(SearchAction.ToggleFavorite(item.movieObj))
                                                }
                                            )
                                        }
                                        is SearchListItem.NativeAdItem -> {
                                            NativeAdItem(nativeAdWrapper = item.wrapper)
                                        }
                                    }
                                }

                                // Bottom Loading Indicator for pagination
                                if (pagination.isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Search Screen - Results State")
@Composable
fun SearchScreenPreview() {
    val sampleMovieObjs = listOf(
        MovieObj("tt0314331", "Love Actually", "2003", "movieObj", "", isFavorite = true),
        MovieObj("tt0110357", "The Lion King", "1994", "movieObj", "", isFavorite = false),
        MovieObj("tt0816692", "Interstellar", "2014", "movieObj", "", isFavorite = false)
    )
    val ad1 = NativeAdWrapper(title = "Get Unlimited Streaming", body = "Watch thousands of hit movies and shows.")
    val ad2 = NativeAdWrapper(title = "Top Rated Sci-Fi Hits", body = "Discover the latest science fiction releases.")
    val items = sampleMovieObjs.toSearchListItems(ad1, ad2)

    val sampleState = SearchState(
        searchText = "love",
        searchResultState = SResult.Success(PaginationState(items = items)),
        nativeAd1 = ad1,
        nativeAd2 = ad2
    )

    JedyAppsAndroidHomeAssignmentTheme {
        SearchScreenContent(
            state = sampleState,
            onAction = {}
        )
    }
}
