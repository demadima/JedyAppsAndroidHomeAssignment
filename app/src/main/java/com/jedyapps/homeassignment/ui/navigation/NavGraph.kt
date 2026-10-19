package com.jedyapps.homeassignment.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.jedyapps.homeassignment.ui.details.DetailScreen
import com.jedyapps.homeassignment.ui.details.DetailViewModel
import com.jedyapps.homeassignment.ui.favorites.FavoriteScreen
import com.jedyapps.homeassignment.ui.favorites.FavoriteViewModel
import com.jedyapps.homeassignment.ui.search.SearchScreen
import com.jedyapps.homeassignment.ui.search.SearchViewModel
import kotlinx.serialization.Serializable

interface NavRoute

// Routes matching WoHu pattern
@Serializable object SearchRoute : NavRoute
@Serializable object FavoriteRoute : NavRoute

// Route with extra arguments/data!
@Serializable data class DetailRoute(
    val imdbId: String
) : NavRoute

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = SearchRoute
    ) {
        composable<SearchRoute> {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                viewModel = viewModel,
                onNavigateToDetail = { imdbId ->
                    navController.navigate(DetailRoute(imdbId = imdbId))
                },
                onNavigateToFavorites = {
                    navController.navigate(FavoriteRoute)
                }
            )
        }

        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            val viewModel: DetailViewModel = hiltViewModel()
            DetailScreen(
                imdbId = route.imdbId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<FavoriteRoute> {
            val viewModel: FavoriteViewModel = hiltViewModel()
            FavoriteScreen(
                viewModel = viewModel,
                onNavigateToDetail = { imdbId ->
                    navController.navigate(DetailRoute(imdbId = imdbId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
