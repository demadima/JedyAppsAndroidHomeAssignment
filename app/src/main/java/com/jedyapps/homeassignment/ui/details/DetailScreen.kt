package com.jedyapps.homeassignment.ui.details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jedyapps.homeassignment.R
import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.domain.model.MovieDetailObj
import com.jedyapps.homeassignment.ui.components.TopBarUI
import com.jedyapps.homeassignment.ui.details.mvi.DetailAction
import com.jedyapps.homeassignment.ui.details.mvi.DetailEvent
import com.jedyapps.homeassignment.ui.theme.JedyAppsAndroidHomeAssignmentTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DetailScreen(
    imdbId: String,
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DetailEvent.ShowToast -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                }
                is DetailEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    val currentDetail = (state.detailState as? SResult.Success)?.data

    DetailScreenContent(
        detailState = state.detailState,
        currentDetail = currentDetail,
        onBackClick = { viewModel.onAction(DetailAction.OnBackClick) },
        onToggleFavorite = { detail -> viewModel.onAction(DetailAction.ToggleFavorite(detail)) }
    )
}

@Composable
fun DetailScreenContent(
    detailState: SResult<MovieDetailObj, *>,
    currentDetail: MovieDetailObj?,
    onBackClick: () -> Unit,
    onToggleFavorite: (MovieDetailObj) -> Unit
) {
    Scaffold(
        topBar = {
            TopBarUI(
                title = stringResource(R.string.detail_screen_title),
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (currentDetail != null) {
                FloatingActionButton(
                    onClick = { onToggleFavorite(currentDetail) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = if (currentDetail.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentDetail.isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (detailState) {
                is SResult.Loading -> {
                    CircularProgressIndicator()
                }
                is SResult.Error -> {
                    Text(
                        text = stringResource(R.string.detail_error_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is SResult.Success -> {
                    MovieDetailContent(detail = detailState.data)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MovieDetailContent(detail: MovieDetailObj) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Poster Image
        AsyncImage(
            model = detail.poster,
            contentDescription = detail.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = detail.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Metadata badges row (Year, Type, Rating)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = detail.type.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = detail.year,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Rating
            if (detail.imdbRating.isNotBlank() && detail.imdbRating != "N/A") {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${detail.imdbRating} / 10",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Genre
        if (detail.genre.isNotBlank() && detail.genre != "N/A") {
            Text(
                text = stringResource(R.string.detail_genre_format, detail.genre),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Director & Actors
        if (detail.director.isNotBlank() && detail.director != "N/A") {
            Text(
                text = stringResource(R.string.detail_director_format, detail.director),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (detail.actors.isNotBlank() && detail.actors != "N/A") {
            Text(
                text = stringResource(R.string.detail_cast_format, detail.actors),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Plot Description
        Text(
            text = stringResource(R.string.detail_plot_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = detail.plot,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB
    }
}

@Preview(showBackground = true, name = "Detail Screen Preview")
@Composable
fun DetailScreenPreview() {
    val sampleDetail = MovieDetailObj(
        imdbID = "tt0314331",
        title = "Love Actually",
        year = "2003",
        type = "movieObj",
        poster = "",
        plot = "Follows the lives of eight very different couples in dealing with their love lives in various loosely interrelated tales all set during a frantic month before Christmas in London, England.",
        genre = "Comedy, Drama, Romance",
        director = "Richard Curtis",
        actors = "Hugh Grant, Martine McCutcheon, Liam Neeson",
        imdbRating = "7.6",
        released = "07 Nov 2003",
        isFavorite = true
    )

    JedyAppsAndroidHomeAssignmentTheme {
        DetailScreenContent(
            detailState = SResult.Success<MovieDetailObj, Nothing>(sampleDetail),
            currentDetail = sampleDetail,
            onBackClick = {},
            onToggleFavorite = {}
        )
    }
}
