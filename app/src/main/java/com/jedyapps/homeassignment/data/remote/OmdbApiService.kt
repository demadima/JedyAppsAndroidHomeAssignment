package com.jedyapps.homeassignment.data.remote

import com.jedyapps.homeassignment.data.model.dto.OmdbDetailResponse
import com.jedyapps.homeassignment.data.model.dto.OmdbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApiService {

    @GET("/")
    suspend fun searchMovies(
        @Query("apikey") apiKey: String = API_KEY,
        @Query("s") searchKeyword: String,
        @Query("page") page: Int = 1
    ): OmdbSearchResponse

    @GET("/")
    suspend fun getMovieDetail(
        @Query("apikey") apiKey: String = API_KEY,
        @Query("i") imdbId: String,
        @Query("plot") plot: String = "full"
    ): OmdbDetailResponse

    companion object {
        const val BASE_URL = "https://www.omdbapi.com/"
        const val API_KEY = "9278e7bc"
    }
}
