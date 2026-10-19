package com.jedyapps.sdk.providers

import android.app.Activity
import android.content.Context
import com.jedyapps.sdk.wrapers.NativeAdWrapper

/**
 * Abstraction interface for ad network providers (e.g. AdMob, Unity, AppLovin).
 * Ensures the SDK architecture is easily scalable for future ad providers.
 */
interface AdProvider {
    val providerName: String

    fun initialize(context: Context)

    fun loadNativeAd(
        adUnitId: String,
        onLoaded: (NativeAdWrapper) -> Unit,
        onFailed: (String) -> Unit
    )

    fun loadInterstitialAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    )

    fun showInterstitialAd(
        activity: Activity,
        onDismissed: () -> Unit
    )

    fun isInterstitialReady(): Boolean
}
