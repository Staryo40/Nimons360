package com.labpro.nimons360.ui.features.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BatteryTracker(private val context: Context) {
    private var call: ((Int?, Boolean?) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent == null) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

            val value = if (level >= 0 && scale > 0) {
                ((level * 100f) / scale).toInt()
            } else {
                null
            }

            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            call?.invoke(value, charging)
        }
    }

    fun start(onUpdate: (Int?, Boolean?) -> Unit) {
        call = onUpdate
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
        call = null
    }
}
