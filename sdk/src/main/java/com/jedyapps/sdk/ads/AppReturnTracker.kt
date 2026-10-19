package com.jedyapps.sdk.ads

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.core.content.edit

/**
 * Tracks app return events using a bounded 0/1 toggle state
 * to trigger the interstitial ad on every second return (2nd, 4th, 6th, etc.)
 * without infinite integer increments or overflow risk.
 */
class AppReturnTracker(
    context: Context,
    private val onTriggerInterstitial: () -> Unit
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AppReturnTracker"
        private const val PREFS_NAME = "jedyapps_sdk_ads_prefs"
        private const val KEY_RETURN_STATE = "app_return_state"
    }

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun startTracking() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        // Bounded 0/1 toggle state: increments by 1 and wraps around via modulo 2
        // State 1 = first return (no ad)
        // State 0 = second return (show ad)
        val currentState = (prefs.getInt(KEY_RETURN_STATE, 0) + 1) % 2
        prefs.edit { putInt(KEY_RETURN_STATE, currentState) }

        Log.d(TAG, "App return state toggle: $currentState (0 means show ad)")

        // Requirement: Show interstitial ad on every second return (2, 4, 6, 8, etc.)
        if (currentState == 0) {
            Log.d(TAG, "User returned to the app for an even return! Triggering interstitial ad.")
            onTriggerInterstitial()
        }
    }
}
