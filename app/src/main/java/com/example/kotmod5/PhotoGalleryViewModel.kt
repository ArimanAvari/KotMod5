package com.example.kotmod5

import android.app.Application
import android.os.Environment
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

    private val picturesDir: File
        get() = requireNotNull(
            getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

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
        val fileName = "IMG_${createTimestamp()}.jpg"
        return File(picturesDir, fileName)
    }

    private fun createTimestamp(): String {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return format.format(Date())
    }
}
