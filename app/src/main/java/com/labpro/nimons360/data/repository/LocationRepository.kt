package com.labpro.nimons360.data.repository

import android.content.Context
import android.net.Uri
import com.labpro.nimons360.data.local.FavoriteLocationDao
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class LocationRepository(
    private val dao: FavoriteLocationDao,
    context: Context? = null,
) {
    private val app = context?.applicationContext
    private val photoDirectory: File?
        get() = app?.let { File(it.filesDir, PHOTO_DIRECTORY) }

    fun observeFavoriteLocations(): Flow<List<FavoriteLocationEntity>> {
        return dao.observeFavoriteLocations()
    }

    suspend fun addFavoriteLocation(
        latitude: Double,
        longitude: Double,
        title: String,
        description: String = "",
        photoPaths: List<String> = emptyList(),
    ): Int {
        val name = title.trim().ifEmpty { DEFAULT_TITLE }
        return dao.insertLocation(
            FavoriteLocationEntity(
                latitude = latitude,
                longitude = longitude,
                title = name,
                description = description.trim(),
                photoPaths = photoPaths,
            )
        ).toInt()
    }

    suspend fun removeFavoriteLocation(id: Int) {
        dao.getLocation(id)?.photoPaths?.forEach(::deletePhoto)
        dao.deleteLocation(id)
    }

    suspend fun updateFavoriteLocation(entity: FavoriteLocationEntity) {
        val previous = dao.getLocation(entity.id)
        dao.updateLocation(
            entity.copy(
                title = entity.title.trim().ifEmpty { DEFAULT_TITLE },
                description = entity.description.trim(),
            )
        )
        val retained = entity.photoPaths.toSet()
        previous?.photoPaths
            ?.filterNot(retained::contains)
            ?.forEach(::deletePhoto)
    }

    fun importPhoto(uri: Uri): String {
        val context = requireNotNull(app) { "Photo storage requires an Android context." }
        val directory = requireNotNull(photoDirectory)
        directory.mkdirs()
        val file = File(directory, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected photo." }
            file.outputStream().use(input::copyTo)
        }
        return file.absolutePath
    }

    fun createCameraPhoto(): File {
        val directory = requireNotNull(photoDirectory) {
            "Photo storage requires an Android context."
        }
        directory.mkdirs()
        return File(directory, "${UUID.randomUUID()}.jpg")
    }

    fun deleteUncommittedPhotos(paths: Collection<String>) {
        paths.forEach(::deletePhoto)
    }

    private fun deletePhoto(path: String) {
        val directory = photoDirectory ?: return
        runCatching {
            val file = File(path)
            if (file.canonicalPath.startsWith(directory.canonicalPath)) {
                file.delete()
            }
        }
    }

    private companion object {
        const val DEFAULT_TITLE = "Favorite Location"
        const val PHOTO_DIRECTORY = "marked_location_photos"
    }
}
