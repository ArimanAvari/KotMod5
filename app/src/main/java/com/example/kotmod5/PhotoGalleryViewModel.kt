package com.example.kotmod5

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhotoItem(
    val name: String,
    val file: File,
    val createdAt: Long
)

data class PhotoGalleryUiState(
    val isLoading: Boolean = true,
    val photos: List<PhotoItem> = emptyList()
)

class PhotoGalleryViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(PhotoGalleryUiState())
        private set

    private val app = getApplication<Application>()

    private val picturesDir: File
        get() = requireNotNull(app.getExternalFilesDir(Environment.DIRECTORY_PICTURES))

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            val photos = withContext(Dispatchers.IO) {
                picturesDir.mkdirs()
                picturesDir.listFiles { file ->
                    file.isFile && file.extension.equals("jpg", ignoreCase = true)
                }
                    ?.sortedByDescending { it.lastModified() }
                    ?.map { file ->
                        PhotoItem(
                            name = file.name,
                            file = file,
                            createdAt = file.lastModified()
                        )
                    }
                    ?: emptyList()
            }

            uiState = PhotoGalleryUiState(
                isLoading = false,
                photos = photos
            )
        }
    }

    fun onPhotoSaved() {
        loadPhotos()
    }

    fun createPhotoFile(): File {
        picturesDir.mkdirs()
        return File(picturesDir, "IMG_${timestamp()}.jpg")
    }

    fun exportPhoto(photo: PhotoItem, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val message = withContext(Dispatchers.IO) {
                runCatching { copyToMediaStore(photo) }
                    .fold(
                        onSuccess = { "Фото добавлено в галерею" },
                        onFailure = { "Не удалось экспортировать фото" }
                    )
            }
            onDone(message)
        }
    }

    private fun copyToMediaStore(photo: PhotoItem) {
        val resolver = app.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, photo.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, photo.createdAt)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Фотоархив"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = requireNotNull(resolver.insert(collection, values))

        try {
            resolver.openOutputStream(uri)?.use { output ->
                photo.file.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: error("Cannot open output stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val update = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(uri, update, null, null)
            }
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun timestamp(): String {
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return formatter.format(Date())
    }
}
