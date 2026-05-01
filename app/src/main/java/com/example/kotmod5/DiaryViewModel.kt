package com.example.kotmod5

import android.app.Application
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

data class DiaryEntry(
    val fileName: String,
    val title: String,
    val content: String,
    val createdAt: Long
) {
    val previewText: String
        get() = content
            .replace("\n", " ")
            .trim()
            .let { text ->
                if (text.length <= 40) {
                    text
                } else {
                    text.take(37) + "..."
                }
            }
}

data class DiaryUiState(
    val isLoading: Boolean = true,
    val entries: List<DiaryEntry> = emptyList()
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(DiaryUiState())
        private set

    private val notesDir: File
        get() = getApplication<Application>().filesDir

    init {
        loadEntries()
    }

    fun loadEntries() {
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) {
                DiaryStorage.loadEntries(notesDir)
            }
            uiState = DiaryUiState(isLoading = false, entries = entries)
        }
    }

    fun saveEntry(
        fileName: String?,
        title: String,
        content: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (fileName == null) {
                    DiaryStorage.createEntry(notesDir, title, content)
                } else {
                    DiaryStorage.updateEntry(notesDir, fileName, title, content)
                }
            } ?: return@launch

            uiState = if (fileName == null) {
                uiState.copy(entries = listOf(result) + uiState.entries)
            } else {
                uiState.copy(
                    entries = uiState.entries.map { entry ->
                        if (entry.fileName == fileName) result else entry
                    }
                )
            }

            onSaved()
        }
    }

    fun deleteEntry(fileName: String) {
        viewModelScope.launch {
            val deleted = withContext(Dispatchers.IO) {
                DiaryStorage.deleteEntry(notesDir, fileName)
            }
            if (!deleted) return@launch

            uiState = uiState.copy(
                entries = uiState.entries.filterNot { it.fileName == fileName }
            )
        }
    }
}

private object DiaryStorage {
    fun loadEntries(directory: File): List<DiaryEntry> {
        return directory.listFiles { file ->
            file.isFile && file.extension == "txt"
        }
            ?.mapNotNull(::readEntry)
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    fun createEntry(directory: File, title: String, content: String): DiaryEntry? {
        val createdAt = System.currentTimeMillis()
        val fileName = buildFileName(createdAt, title)
        val file = File(directory, fileName)
        val entry = DiaryEntry(
            fileName = fileName,
            title = title.trim(),
            content = content.trim(),
            createdAt = createdAt
        )

        file.writeText(serialize(entry))
        return readEntry(file)
    }

    fun updateEntry(
        directory: File,
        oldFileName: String,
        title: String,
        content: String
    ): DiaryEntry? {
        val oldFile = File(directory, oldFileName)
        if (!oldFile.exists()) return null

        val oldEntry = readEntry(oldFile) ?: return null
        val newFileName = buildFileName(oldEntry.createdAt, title)
        val targetFile = File(directory, newFileName)

        val updatedEntry = oldEntry.copy(
            fileName = newFileName,
            title = title.trim(),
            content = content.trim()
        )

        if (oldFileName != newFileName) {
            if (!oldFile.renameTo(targetFile)) {
                targetFile.writeText(serialize(updatedEntry))
                oldFile.delete()
            }
        }

        targetFile.writeText(serialize(updatedEntry))
        return readEntry(targetFile)
    }

    fun deleteEntry(directory: File, fileName: String): Boolean {
        val file = File(directory, fileName)
        return file.exists() && file.delete()
    }

    private fun readEntry(file: File): DiaryEntry? {
        val parts = file.readText().split("\n", limit = 3)
        if (parts.size < 2) return null

        val createdAt = parts[0].toLongOrNull() ?: file.lastModified()
        val title = parts[1]
        val content = parts.getOrElse(2) { "" }

        return DiaryEntry(
            fileName = file.name,
            title = title,
            content = content,
            createdAt = createdAt
        )
    }

    private fun serialize(entry: DiaryEntry): String {
        return buildString {
            append(entry.createdAt)
            append('\n')
            append(entry.title)
            append('\n')
            append(entry.content)
        }
    }

    private fun buildFileName(timestamp: Long, title: String): String {
        val cleanedTitle = title
            .trim()
            .replace(Regex("[^\\p{L}\\p{N}]+"), "_")
            .trim('_')
            .take(24)

        return if (cleanedTitle.isBlank()) {
            "${timestamp}.txt"
        } else {
            "${timestamp}_${cleanedTitle}.txt"
        }
    }
}

fun formatDiaryDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
