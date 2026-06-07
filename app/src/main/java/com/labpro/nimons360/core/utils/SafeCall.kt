package com.labpro.nimons360.core.utils

import android.util.Log
import com.labpro.nimons360.data.model.NetworkResult

suspend inline fun <T> safeCall(
    tag: String,
    crossinline block: suspend () -> NetworkResult<T>,
): NetworkResult<T> = try {
    block()
} catch (e: Exception) {
    val msg = e.message ?: "Unknown network error."
    Log.e(tag, "Network error: $msg", e)
    NetworkResult.Error("Unable to complete the request. Please try again.")
}
