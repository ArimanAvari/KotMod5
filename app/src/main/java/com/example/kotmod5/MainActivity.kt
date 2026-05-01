package com.example.kotmod5

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kotmod5.ui.theme.KotMod5Theme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotMod5Theme {
                PhotoGalleryApp()
            }
        }
    }
}

@Composable
private fun PhotoGalleryApp(viewModel: PhotoGalleryViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    var openedPhotoName by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var pendingExportPhotoName by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSaved ->
        val file = pendingPhotoFile
        if (isSaved && file != null) {
            viewModel.onPhotoSaved()
        } else {
            file?.delete()
        }
        pendingPhotoFile = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Камера недоступна без разрешения")
            }
            return@rememberLauncherForActivityResult
        }

        val file = viewModel.createPhotoFile()
        pendingPhotoFile = file
        takePictureLauncher.launch(file.toContentUri(context))
    }

    val exportPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val photo = uiState.photos.firstOrNull { it.name == pendingExportPhotoName }
        if (!granted || photo == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Не удалось получить доступ к галерее")
            }
            pendingExportPhotoName = null
            return@rememberLauncherForActivityResult
        }

        viewModel.exportPhoto(photo) { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
        pendingExportPhotoName = null
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun startCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val file = viewModel.createPhotoFile()
            pendingPhotoFile = file
            takePictureLauncher.launch(file.toContentUri(context))
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun exportPhoto(photo: PhotoItem) {
        val needsLegacyPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
        val hasLegacyPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (needsLegacyPermission && !hasLegacyPermission) {
            pendingExportPhotoName = photo.name
            exportPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        viewModel.exportPhoto(photo) { message ->
            showMessage(message)
        }
    }

    val selectedPhoto = uiState.photos.firstOrNull { it.name == openedPhotoName }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (openedPhotoName == null) {
                FloatingActionButton(onClick = ::startCamera) {
                    Text(
                        text = "Фото",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (openedPhotoName == null) {
                GalleryScreen(
                    uiState = uiState,
                    onTakePhotoClick = ::startCamera,
                    onOpenPhoto = { photo -> openedPhotoName = photo.name },
                    onExportPhoto = ::exportPhoto
                )
            } else {
                selectedPhoto?.let { photo ->
                    PhotoPreviewScreen(
                        photo = photo,
                        onBack = { openedPhotoName = null },
                        onExportClick = { exportPhoto(photo) }
                    )
                } ?: run {
                    openedPhotoName = null
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryScreen(
    uiState: PhotoGalleryUiState,
    onTakePhotoClick: () -> Unit,
    onOpenPhoto: (PhotoItem) -> Unit,
    onExportPhoto: (PhotoItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Мои фото") }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Загрузка фотографий...")
                }
            }

            uiState.photos.isEmpty() -> {
                EmptyGalleryScreen(
                    modifier = Modifier.padding(innerPadding),
                    onTakePhotoClick = onTakePhotoClick
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.photos,
                        key = { photo -> photo.name }
                    ) { photo ->
                        PhotoGridCard(
                            photo = photo,
                            onOpenPhoto = { onOpenPhoto(photo) },
                            onExportPhoto = { onExportPhoto(photo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGalleryScreen(
    modifier: Modifier = Modifier,
    onTakePhotoClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "У вас пока нет фото",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Сделайте первое фото, и оно появится в галерее",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onTakePhotoClick) {
                Text(text = "Сделать первое фото")
            }
        }
    }
}

@Composable
private fun PhotoGridCard(
    photo: PhotoItem,
    onOpenPhoto: () -> Unit,
    onExportPhoto: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            AsyncImage(
                model = photo.file,
                contentDescription = photo.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clickable(onClick = onOpenPhoto),
                contentScale = ContentScale.Crop
            )

            Text(
                text = photo.name,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        ) {
            TextButton(
                onClick = { menuExpanded = true }
            ) {
                Text(text = "⋮")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Открыть") },
                    onClick = {
                        menuExpanded = false
                        onOpenPhoto()
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Экспорт в галерею") },
                    onClick = {
                        menuExpanded = false
                        onExportPhoto()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoPreviewScreen(
    photo: PhotoItem,
    onBack: () -> Unit,
    onExportClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Просмотр") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = photo.file,
                contentDescription = photo.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Text(
                text = photo.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Сделано: ${formatPhotoDate(photo.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Экспорт в галерею")
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Назад")
            }
        }
    }
}

private fun File.toContentUri(context: Context): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        this
    )
}

private fun formatPhotoDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
