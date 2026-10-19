package com.jedyapps.sdk.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.jedyapps.sdk.ads.AdManager.isPendingInterstitialShow
import com.jedyapps.sdk.providers.AdMobProvider
import com.jedyapps.sdk.providers.AdProvider
import com.jedyapps.sdk.wrapers.NativeAdWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Singleton / Facade managing ad logic in the SDK module.
 * All ads (Interstitial and Native) start preloading immediately at app launch (MainApplication).
 */
object AdManager {
    private const val TAG = "AdManager"

    private var activeProvider: AdProvider = AdMobProvider()
    private var returnTracker: AppReturnTracker? = null
    private var isInitialized = false

    private var currentActivityRef: WeakReference<Activity>? = null
    private var isPendingInterstitialShow = false

    // Preloaded native ads StateFlows (loaded right at app launch)
    private val _nativeAd1 = MutableStateFlow<NativeAdWrapper?>(null)
    val nativeAd1: StateFlow<NativeAdWrapper?> = _nativeAd1.asStateFlow()

    private val _nativeAd2 = MutableStateFlow<NativeAdWrapper?>(null)
    val nativeAd2: StateFlow<NativeAdWrapper?> = _nativeAd2.asStateFlow()

    /**
     * Set custom ad provider (for scalability / future ad network integration).
     */
    fun setAdProvider(provider: AdProvider) {
        activeProvider = provider
    }

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appCtx = context.applicationContext
        Log.d(TAG, "Initializing AdManager and preloading all ads at APP START with provider: ${activeProvider.providerName}")
        activeProvider.initialize(appCtx)

        // 1. Preload interstitial ad immediately at app startup
        preloadInterstitial()

        // 2. Preload native ads immediately at app startup
        preloadNativeAds()

        // 3. Setup AppReturnTracker for every second app return (2, 4, 6...)
        returnTracker = AppReturnTracker(appCtx) {
            isPendingInterstitialShow = true
        }.also {
            it.startTracking()
        }
    }

    private fun preloadInterstitial() {
        if (activeProvider.isInterstitialReady()) {
            return
        }

        activeProvider.loadInterstitialAd(
            adUnitId = AdMobProvider.TEST_INTERSTITIAL_AD_UNIT_ID,
            onLoaded = {
                Log.d(TAG, "Interstitial ad preloaded successfully from network.")
                currentActivityRef?.get()?.let { activity ->
                    showInterstitialIfReady(activity)
                }
            },
            onFailed = { err ->
                Log.e(TAG, "Failed to preload interstitial ad: $err")
                // If an ad show is pending, retry loading after failure
                if (isPendingInterstitialShow) {
                    Log.d(TAG, "Retrying interstitial preload due to pending show request...")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        preloadInterstitial()
                    }, 3000L)
                }
            }
        )
    }

    private fun preloadNativeAds() {
        Log.d(TAG, "Preloading Native Ads at app startup...")
        activeProvider.loadNativeAd(
            adUnitId = AdMobProvider.TEST_NATIVE_AD_UNIT_ID,
            onLoaded = { adWrapper ->
                Log.d(TAG, "Native Ad 1 preloaded successfully at app launch.")
                _nativeAd1.value = adWrapper
            },
            onFailed = { err ->
                Log.e(TAG, "Failed to preload Native Ad 1: $err")
            }
        )

        activeProvider.loadNativeAd(
            adUnitId = AdMobProvider.TEST_NATIVE_AD_UNIT_ID,
            onLoaded = { adWrapper ->
                Log.d(TAG, "Native Ad 2 preloaded successfully at app launch.")
                _nativeAd2.value = adWrapper
            },
            onFailed = { err ->
                Log.e(TAG, "Failed to preload Native Ad 2: $err")
            }
        )
    }

    fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        showInterstitialIfReady(activity)
    }

    fun onActivityPaused() {
        currentActivityRef?.clear()
        currentActivityRef = null
    }

    private fun showInterstitialIfReady(activity: Activity) {
        if (isPendingInterstitialShow) {
            if (activeProvider.isInterstitialReady()) {
                isPendingInterstitialShow = false
                activeProvider.showInterstitialAd(activity) {
                    Log.d(TAG, "Interstitial ad dismissed. Preloading next interstitial...")
                    preloadInterstitial()
                }
            } else {
                Log.d(TAG, "Interstitial ad is pending, but not ready yet. Ensuring preload...")
                preloadInterstitial()
            }
        }
    }
}
