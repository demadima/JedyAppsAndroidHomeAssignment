package com.jedyapps.homeassignment.ui.search.mvi

import com.jedyapps.homeassignment.data.sealed.SResult
import com.jedyapps.homeassignment.domain.model.pagination.PaginationState
import com.jedyapps.homeassignment.ui.search.model.SearchListItem
import com.jedyapps.sdk.wrapers.NativeAdWrapper

data class SearchState(
    val searchText: String = "love",
    val searchResultState: SResult<PaginationState<List<SearchListItem>>, Any?> = SResult.Initial,
    val nativeAd1: NativeAdWrapper? = null,
    val nativeAd2: NativeAdWrapper? = null
)
