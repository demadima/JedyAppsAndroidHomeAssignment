package com.jedyapps.sdk.ui

import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.google.android.gms.ads.nativead.NativeAdView
import com.jedyapps.sdk.R
import com.jedyapps.sdk.wrapers.NativeAdWrapper

@Composable
fun NativeAdItem(
    nativeAdWrapper: NativeAdWrapper?,
    modifier: Modifier = Modifier
) {
    if (nativeAdWrapper?.adMobNativeAd != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val nativeAd = nativeAdWrapper.adMobNativeAd
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                factory = { context ->
                    val adView = NativeAdView(context)

                    val container = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val headerRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val adBadge = TextView(context).apply {
                        text = context.getString(R.string.ad_badge)
                        textSize = 10f
                        setPadding(12, 4, 12, 4)
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor("#FF9800".toColorInt())
                    }
                    headerRow.addView(adBadge)

                    val iconView = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                            setMargins(16, 0, 16, 0)
                        }
                    }
                    adView.iconView = iconView
                    headerRow.addView(iconView)

                    val headlineView = TextView(context).apply {
                        textSize = 16f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(android.graphics.Color.BLACK)
                    }
                    adView.headlineView = headlineView
                    headerRow.addView(headlineView)

                    container.addView(headerRow)

                    val bodyView = TextView(context).apply {
                        textSize = 13f
                        maxLines = 2
                        setTextColor(android.graphics.Color.DKGRAY)
                        setPadding(0, 12, 0, 12)
                    }
                    adView.bodyView = bodyView
                    container.addView(bodyView)

                    val ctaButton = Button(context).apply {
                        textSize = 14f
                    }
                    adView.callToActionView = ctaButton
                    container.addView(ctaButton)

                    adView.addView(container)

                    // Bind data
                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    (adView.bodyView as? TextView)?.text = nativeAd.body ?: ""
                    (adView.callToActionView as? Button)?.text =
                        nativeAd.callToAction ?: context.getString(R.string.learn_more)

                    if (nativeAd.icon != null) {
                        (adView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
                    }

                    adView.setNativeAd(nativeAd)
                    adView
                }
            )
        }
    }
}
