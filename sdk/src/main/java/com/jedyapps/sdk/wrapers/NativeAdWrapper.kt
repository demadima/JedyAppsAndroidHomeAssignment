package com.jedyapps.sdk.wrapers

import com.google.android.gms.ads.nativead.NativeAd

/**
 * Scalable wrapper around native ad objects.
 * Allows support for different ad networks without exposing network-specific types directly to UI if needed.
 */
data class NativeAdWrapper(
    val adMobNativeAd: NativeAd? = null,
    val title: String = "Sponsored Content",
    val body: String = "Check out this promoted item.",
    val callToAction: String = "Install",
    val isFallback: Boolean = false
)
