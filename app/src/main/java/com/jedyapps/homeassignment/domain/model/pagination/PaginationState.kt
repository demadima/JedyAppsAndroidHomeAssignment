package com.jedyapps.homeassignment.domain.model.pagination

data class PaginationState<T>(
    val items: T,
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val endReached: Boolean = false
)
