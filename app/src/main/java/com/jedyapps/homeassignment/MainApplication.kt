package com.jedyapps.homeassignment

import android.app.Application
import com.jedyapps.sdk.ads.AdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}
