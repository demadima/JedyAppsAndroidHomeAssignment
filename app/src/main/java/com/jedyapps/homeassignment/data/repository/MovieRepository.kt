package com.jedyapps.homeassignment.data.repository

import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.domain.model.MovieObj
import com.jedyapps.homeassignment.domain.model.MovieDetailObj
import com.jedyapps.homeassignment.domain.model.pagination.PaginationState
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun searchMovies(keyword: String, page: Int): Flow<SResult<PaginationState<List<MovieObj>>, Any?>>
    suspend fun getMovieDetail(imdbId: String): Flow<SResult<MovieDetailObj, Any?>>
    fun getFavoriteMovies(): Flow<List<MovieObj>>
    fun isFavoriteFlow(imdbId: String): Flow<Boolean>
    suspend fun toggleFavorite(movieObj: MovieObj)
    suspend fun addFavoriteDetail(movieDetailObj: MovieDetailObj)
    suspend fun removeFavorite(imdbId: String)
}
