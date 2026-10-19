package com.jedyapps.homeassignment.ui.search.model

import com.jedyapps.homeassignment.domain.model.MovieObj
import com.jedyapps.sdk.wrapers.NativeAdWrapper

sealed class SearchListItem {
    data class MovieItem(val movieObj: MovieObj) : SearchListItem()
    data class NativeAdItem(val id: String, val wrapper: NativeAdWrapper?) : SearchListItem()
}

/**
 * Inserts native ads at index 1 and index 3 in the search list when sufficient items exist (size >= 3).
 */
fun List<MovieObj>.toSearchListItems(ad1: NativeAdWrapper?, ad2: NativeAdWrapper?): List<SearchListItem> {
    if (isEmpty()) return emptyList()
    val list = mutableListOf<SearchListItem>()

    // Index 0: First MovieObj
    list.add(SearchListItem.MovieItem(this[0]))

    if (size >= 3) {
        // Index 1: First Native Ad
        list.add(SearchListItem.NativeAdItem("ad1", ad1))
        // Index 2: Second MovieObj
        list.add(SearchListItem.MovieItem(this[1]))
        // Index 3: Second Native Ad
        list.add(SearchListItem.NativeAdItem("ad2", ad2))
        // Remaining movies added from index 2 onwards
        for (i in 2 until size) {
            list.add(SearchListItem.MovieItem(this[i]))
        }
    } else {
        // If less than 3 items, add remaining movies without inserting any native ads
        for (i in 1 until size) {
            list.add(SearchListItem.MovieItem(this[i]))
        }
    }

    return list
}
