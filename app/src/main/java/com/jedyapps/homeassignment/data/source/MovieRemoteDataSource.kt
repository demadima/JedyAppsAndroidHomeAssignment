package com.jedyapps.homeassignment.data.source

import com.jedyapps.homeassignment.data.model.dto.OmdbDetailResponse
import com.jedyapps.homeassignment.data.model.dto.OmdbMovieDto
import com.jedyapps.homeassignment.data.remote.OmdbApiService
import com.jedyapps.homeassignment.domain.model.pagination.PaginationState
import javax.inject.Inject

class MovieRemoteDataSource @Inject constructor(
    private val apiService: OmdbApiService
) {
    suspend fun searchMovies(keyword: String, page: Int): PaginationState<List<OmdbMovieDto>> {
        val response = apiService.searchMovies(searchKeyword = keyword, page = page)
        if (response.response.equals("True", ignoreCase = true) && response.search != null) {
            val totalResults = response.totalResults?.toIntOrNull() ?: response.search.size
            val endReached = (page * 10) >= totalResults || response.search.isEmpty()
            return PaginationState(
                items = response.search,
                currentPage = page,
                endReached = endReached
            )
        } else {
            throw Exception(response.error ?: "No movies found")
        }
    }

    suspend fun getMovieDetail(imdbId: String): OmdbDetailResponse {
        val response = apiService.getMovieDetail(imdbId = imdbId)
        if (response.response.equals("True", ignoreCase = true)) {
            return response
        } else {
            throw Exception(response.error ?: "Failed to load details")
        }
    }
}
