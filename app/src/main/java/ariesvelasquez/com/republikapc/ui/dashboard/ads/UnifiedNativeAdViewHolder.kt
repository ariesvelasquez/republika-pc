package ariesvelasquez.com.republikapc.ui.dashboard.ads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd


class UnifiedNativeAdViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var adView: UnifiedNativeAdView = view.findViewById(R.id.ad_view) as UnifiedNativeAdView

    init {

        // The MediaView will display a video asset if one is present in the ad, and the
        // first image asset otherwise.
        adView.mediaView = adView.findViewById(R.id.ad_media) as MediaView

        // Register the view used for each individual asset.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
    }

    fun bind(nativeAd: UnifiedNativeAd) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.

        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).setText(nativeAd.callToAction)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        val icon = nativeAd.icon

        if (icon == null) {
            adView.iconView.visibility = View.INVISIBLE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.setVisibility(View.VISIBLE)
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd)
    }

    companion object {
        fun create(parent: ViewGroup): UnifiedNativeAdViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ad_unified, parent, false)
            return UnifiedNativeAdViewHolder(view)
        }
    }
}