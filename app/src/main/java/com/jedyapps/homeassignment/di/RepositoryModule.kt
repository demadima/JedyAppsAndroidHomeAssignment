package com.jedyapps.homeassignment.di

import com.jedyapps.homeassignment.data.local.dao.FavoriteMovieDao
import com.jedyapps.homeassignment.data.remote.OmdbApiService
import com.jedyapps.homeassignment.data.repository.MovieRepository
import com.jedyapps.homeassignment.data.repository.MovieRepositoryImpl
import com.jedyapps.homeassignment.data.source.MovieLocalDataSource
import com.jedyapps.homeassignment.data.source.MovieRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMovieRemoteDataSource(apiService: OmdbApiService): MovieRemoteDataSource {
        return MovieRemoteDataSource(apiService)
    }

    @Provides
    @Singleton
    fun provideMovieLocalDataSource(favoriteDao: FavoriteMovieDao): MovieLocalDataSource {
        return MovieLocalDataSource(favoriteDao)
    }

    @Provides
    @Singleton
    fun provideMovieRepository(
        remoteDataSource: MovieRemoteDataSource,
        localDataSource: MovieLocalDataSource
    ): MovieRepository {
        return MovieRepositoryImpl(remoteDataSource, localDataSource)
    }
}
