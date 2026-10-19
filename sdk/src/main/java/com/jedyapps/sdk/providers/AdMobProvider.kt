package com.jedyapps.sdk.providers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jedyapps.sdk.wrapers.NativeAdWrapper

class AdMobProvider : AdProvider {

    override val providerName: String = "AdMob"

    private var appContext: Context? = null
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading: Boolean = false

    companion object {
        private const val TAG = "AdMobProvider"

        // Official Google AdMob Test Ad Unit IDs
        const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    override fun initialize(context: Context) {
        val appCtx = context.applicationContext
        appContext = appCtx
        MobileAds.initialize(appCtx) { status ->
            Log.d(TAG, "AdMob MobileAds initialized: ${status.adapterStatusMap}")
        }
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    override fun loadNativeAd(
        adUnitId: String,
        onLoaded: (NativeAdWrapper) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val context = appContext
        if (context == null) {
            Log.e(TAG, "Cannot load native ad: AdMobProvider not initialized")
            onFailed("Provider not initialized")
            return
        }

        val effectiveAdUnitId = adUnitId.ifBlank { TEST_NATIVE_AD_UNIT_ID }
        Log.d(TAG, "Loading Native Ad with unit ID: $effectiveAdUnitId")

        val adLoader = AdLoader.Builder(context, effectiveAdUnitId)
            .forNativeAd { nativeAd ->
                Log.d(TAG, "Native Ad successfully loaded: ${nativeAd.headline}")
                onLoaded(
                    NativeAdWrapper(
                        adMobNativeAd = nativeAd,
                        title = nativeAd.headline ?: "Promoted"
                    )
                )
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Native Ad failed to load: ${loadAdError.message}")
                    onFailed(loadAdError.message)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun loadInterstitialAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val context = appContext
        if (context == null) {
            Log.e(TAG, "Cannot load interstitial ad: AdMobProvider not initialized")
            onFailed("Provider not initialized")
            return
        }

        if (interstitialAd != null || isInterstitialLoading) {
            Log.d(TAG, "Interstitial ad already exists or is loading.")
            return
        }

        val effectiveAdUnitId = adUnitId.ifBlank { TEST_INTERSTITIAL_AD_UNIT_ID }
        isInterstitialLoading = true
        Log.d(TAG, "Loading Interstitial Ad with unit ID: $effectiveAdUnitId")

        InterstitialAd.load(
            context,
            effectiveAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad loaded.")
                    interstitialAd = ad
                    isInterstitialLoading = false
                    onLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                    onFailed(loadAdError.message)
                }
            }
        )
    }

    override fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad is not ready to be shown.")
            onDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad dismissed.")
                interstitialAd = null
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial Ad failed to show: ${adError.message}")
                interstitialAd = null
                onDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad showed fullscreen content.")
            }
        }

        ad.show(activity)
    }

    override fun isInterstitialReady(): Boolean = interstitialAd != null
}
