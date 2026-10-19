package com.jedyapps.homeassignment.data.repository

import com.jedyapps.homeassignment.R
import com.jedyapps.homeassignment.data.model.dto.OmdbDetailResponse
import com.jedyapps.homeassignment.data.model.dto.OmdbMovieDto
import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.data.sealed.UIText
import com.jedyapps.homeassignment.data.source.MovieLocalDataSource
import com.jedyapps.homeassignment.data.source.MovieRemoteDataSource
import com.jedyapps.homeassignment.domain.model.MovieDetailObj
import com.jedyapps.homeassignment.domain.model.MovieObj
import com.jedyapps.homeassignment.domain.model.pagination.PaginationState
import com.jedyapps.homeassignment.domain.model.toDomain
import com.jedyapps.homeassignment.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class MovieRepositoryImpl(
    private val remoteDataSource: MovieRemoteDataSource,
    private val localDataSource: MovieLocalDataSource
) : MovieRepository {

    override suspend fun searchMovies(keyword: String, page: Int): Flow<SResult<PaginationState<List<MovieObj>>, Any?>> {
        return flow {
            emit(remoteDataSource.searchMovies(keyword = keyword, page = page))
        }.map<PaginationState<List<OmdbMovieDto>>, SResult<PaginationState<List<MovieObj>>, Any?>> { pageResult ->
            val movieObjs = pageResult.items.map { dto ->
                val isFav = localDataSource.isFavorite(dto.imdbID)
                dto.toDomain(isFavorite = isFav)
            }
            SResult.Success(
                PaginationState(
                    items = movieObjs,
                    currentPage = page,
                    endReached = pageResult.endReached
                )
            )
        }
        .onStart { emit(SResult.Loading) }
        .catch { e ->
            val errorText = e.localizedMessage?.let { UIText.DynamicString(it) } ?: UIText.StringResource(R.string.error_no_movies_found)
            emit(SResult.Error<PaginationState<List<MovieObj>>, Any?>(errorText))
        }
    }

    override suspend fun getMovieDetail(imdbId: String): Flow<SResult<MovieDetailObj, Any?>> {
        return flow {
            emit(remoteDataSource.getMovieDetail(imdbId = imdbId))
        }.map<OmdbDetailResponse, SResult<MovieDetailObj, Any?>> { response ->
            val isFav = localDataSource.isFavorite(imdbId)
            val detail = response.toDomain(isFavorite = isFav)
            SResult.Success(detail)
        }
        .onStart { emit(SResult.Loading as SResult<MovieDetailObj, Any?>) }
        .catch { e ->
            val errorText = e.localizedMessage?.let { UIText.DynamicString(it) } ?: UIText.StringResource(R.string.error_generic)
            emit(SResult.Error<MovieDetailObj, Any?>(errorText))
        }
    }

    override fun getFavoriteMovies(): Flow<List<MovieObj>> {
        return localDataSource.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavoriteFlow(imdbId: String): Flow<Boolean> {
        return localDataSource.isFavoriteFlow(imdbId)
    }

    override suspend fun toggleFavorite(movieObj: MovieObj) {
        val isFav = localDataSource.isFavorite(movieObj.imdbID)
        if (isFav) {
            localDataSource.deleteFavoriteById(movieObj.imdbID)
        } else {
            localDataSource.insertFavorite(movieObj.toEntity())
        }
    }

    override suspend fun addFavoriteDetail(movieDetailObj: MovieDetailObj) {
        localDataSource.insertFavorite(movieDetailObj.toEntity())
    }

    override suspend fun removeFavorite(imdbId: String) {
        localDataSource.deleteFavoriteById(imdbId)
    }
}
