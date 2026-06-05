package com.labpro.nimons360.core.utils

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object InstagramStoryShareHelper {
    private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    private const val ADD_TO_STORY_ACTION = "com.instagram.share.ADD_TO_STORY"
    private const val IMAGE_MIME_TYPE = "image/png"

    fun captureView(view: View): Bitmap {
        require(view.width > 0 && view.height > 0) { "View has not been laid out." }

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun writeStoryImage(context: Context, bitmap: Bitmap): File {
        val directory = File(context.cacheDir, "instagram_story").apply {
            if (!exists()) mkdirs()
        }
        val file = File(directory, "map_story.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return file
    }

    fun shareToInstagramStory(
        activity: Activity,
        imageFile: File,
        facebookAppId: String,
    ): Boolean {
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            imageFile,
        )

        val intent = Intent(ADD_TO_STORY_ACTION).apply {
            setPackage(INSTAGRAM_PACKAGE)
            setDataAndType(uri, IMAGE_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(activity.contentResolver, imageFile.name, uri)
            if (facebookAppId.isNotBlank()) {
                putExtra("source_application", facebookAppId)
            }
        }

        if (intent.resolveActivity(activity.packageManager) == null) {
            return false
        }

        activity.grantUriPermission(
            INSTAGRAM_PACKAGE,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        activity.startActivity(intent)
        return true
    }
}
