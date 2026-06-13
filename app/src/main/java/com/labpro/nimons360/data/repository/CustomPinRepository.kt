package com.labpro.nimons360.data.repository

import android.content.Context
import com.labpro.nimons360.data.model.map.CustomPin
import java.io.File

class CustomPinRepository(context: Context) {
    private val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }

    fun file(pin: CustomPin): File = File(directory, "${pin.id}.png")

    fun temporaryFile(pin: CustomPin): File = File(directory, "${pin.id}.download")

    fun isDownloaded(pin: CustomPin): Boolean = file(pin).let { it.isFile && it.length() > 0L }

    fun isDownloading(pin: CustomPin): Boolean = temporaryFile(pin).exists()

    companion object {
        private const val DIRECTORY = "custom_pins"
    }
}
