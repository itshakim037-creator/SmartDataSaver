package com.smartdatasaver

import android.app.Activity
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var usageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 50, 30, 30)

        val title = TextView(this)
        title.text = "Smart Data Saver"
        title.textSize = 28f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 20, 0, 40)

        statusText = TextView(this)
        statusText.textSize = 20f
        statusText.setTextColor(Color.WHITE)
        statusText.setPadding(0, 20, 0, 20)

        usageText = TextView(this)
        usageText.textSize = 18f
        usageText.setTextColor(Color.WHITE)
        usageText.setPadding(0, 20, 0, 30)

        val settings = TextView(this)
        settings.text = "⚙ Data Usage Settings"
        settings.textSize = 18f
        settings.setTextColor(Color.WHITE)
        settings.setPadding(20, 25, 20, 25)

        settings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DATA_USAGE_SETTINGS))
        }

        layout.setBackgroundColor(Color.rgb(10, 18, 30))

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(usageText)
        layout.addView(settings)

        setContentView(layout)

        updateNetworkStatus()
        updateDataUsage()
    }

    private fun updateNetworkStatus() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)

        statusText.text = when {
            capabilities == null -> "🔴 Internet: Disconnected"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                "🟢 Internet: Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                "🟢 Internet: Mobile Data"
            else -> "🟡 Internet: Connected"
        }
    }

    private fun updateDataUsage() {
        try {
            val nsm = getSystemService(Context.NETWORK_STATS_SERVICE)
                    as NetworkStatsManager

            val end = System.currentTimeMillis()
            val start = end - 24L * 60L * 60L * 1000L

            var total = 0L

            val mobile = nsm.querySummary(
                ConnectivityManager.TYPE_MOBILE,
                null,
                start,
                end
            )

            val bucket = NetworkStats.Bucket()

            while (mobile.hasNextBucket()) {
                mobile.getNextBucket(bucket)
                total += bucket.rxBytes + bucket.txBytes
            }

            mobile.close()

            val mb = total / (1024.0 * 1024.0)

            usageText.text =
                "📊 Last 24 hours\nMobile Data Used: %.2f MB".format(mb)

        } catch (e: Exception) {
            usageText.text =
                "📊 Data usage দেখতে Usage Access অনুমতি প্রয়োজন।"
        }
    }
}
