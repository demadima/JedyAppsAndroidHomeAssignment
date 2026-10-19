package com.jedyapps.homeassignment.data.source

import com.jedyapps.homeassignment.data.local.dao.FavoriteMovieDao
import com.jedyapps.homeassignment.data.local.entity.FavoriteMovieEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieLocalDataSource @Inject constructor(
    private val favoriteDao: FavoriteMovieDao
) {
    suspend fun isFavorite(imdbId: String): Boolean {
        return favoriteDao.isFavorite(imdbId)
    }

    fun isFavoriteFlow(imdbId: String): Flow<Boolean> {
        return favoriteDao.isFavoriteFlow(imdbId)
    }

    fun getAllFavorites(): Flow<List<FavoriteMovieEntity>> {
        return favoriteDao.getAllFavorites()
    }

    suspend fun insertFavorite(entity: FavoriteMovieEntity) {
        favoriteDao.insertFavorite(entity)
    }

    suspend fun deleteFavoriteById(imdbId: String) {
        favoriteDao.deleteFavoriteById(imdbId)
    }
}
