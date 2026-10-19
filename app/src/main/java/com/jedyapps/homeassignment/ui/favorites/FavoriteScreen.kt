package com.jedyapps.homeassignment.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jedyapps.homeassignment.R
import com.jedyapps.homeassignment.domain.model.MovieObj
import com.jedyapps.homeassignment.ui.components.MovieCardUI
import com.jedyapps.homeassignment.ui.components.TopBarUI
import com.jedyapps.homeassignment.ui.favorites.mvi.FavoriteAction
import com.jedyapps.homeassignment.ui.favorites.mvi.FavoriteEvent
import com.jedyapps.homeassignment.ui.favorites.mvi.FavoriteState
import com.jedyapps.homeassignment.ui.theme.JedyAppsAndroidHomeAssignmentTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is FavoriteEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.imdbId)
                }
                is FavoriteEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    FavoriteScreenContent(
        state = state,
        onAction = { viewModel.onAction(it) }
    )
}

@Composable
fun FavoriteScreenContent(
    state: FavoriteState,
    onAction: (FavoriteAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopBarUI(
                title = stringResource(R.string.favorites_screen_title),
                onBackClick = { onAction(FavoriteAction.OnBackClick) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (state.favoriteMovieObjs.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_favorites_saved),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = state.favoriteMovieObjs,
                        key = { movie -> movie.imdbID }
                    ) { movie ->
                        MovieCardUI(
                            movieObj = movie,
                            onClick = {
                                onAction(FavoriteAction.OnMovieClick(movie.imdbID))
                            },
                            onFavoriteToggle = {
                                onAction(FavoriteAction.ToggleFavorite(movie))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Favorites Screen Preview")
@Composable
fun FavoriteScreenPreview() {
    val sampleFavorites = listOf(
        MovieObj("tt0314331", "Love Actually", "2003", "movieObj", "", isFavorite = true),
        MovieObj("tt0110357", "The Lion King", "1994", "movieObj", "", isFavorite = true)
    )

    JedyAppsAndroidHomeAssignmentTheme {
        FavoriteScreenContent(
            state = FavoriteState(favoriteMovieObjs = sampleFavorites),
            onAction = {}
        )
    }
}
