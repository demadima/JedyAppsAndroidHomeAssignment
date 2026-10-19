package com.jedyapps.homeassignment.data.model.dto

import com.google.gson.annotations.SerializedName

data class OmdbMovieDto(
    @SerializedName("Title") val title: String?,
    @SerializedName("Year") val year: String?,
    @SerializedName("imdbID") val imdbID: String,
    @SerializedName("Type") val type: String?,
    @SerializedName("Poster") val poster: String?
)
