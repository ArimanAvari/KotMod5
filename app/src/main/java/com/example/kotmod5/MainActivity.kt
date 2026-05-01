package com.example.kotmod5

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kotmod5.ui.theme.KotMod5Theme
import java.io.File

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
    val uiState = viewModel.uiState
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSaved ->
        val photoFile = pendingPhotoFile
        if (isSaved && photoFile != null) {
            viewModel.onPhotoSaved()
        } else {
            photoFile?.delete()
        }
        pendingPhotoFile = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            permissionMessage = "Без доступа к камере фото сделать не получится"
            return@rememberLauncherForActivityResult
        }

        val file = viewModel.createPhotoFile()
        pendingPhotoFile = file
        val uri = file.toContentUri(context)
        takePictureLauncher.launch(uri)
    }

    fun launchCamera() {
        permissionMessage = null
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val file = viewModel.createPhotoFile()
            pendingPhotoFile = file
            val uri = file.toContentUri(context)
            takePictureLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        PhotoGalleryScreen(
            uiState = uiState,
            permissionMessage = permissionMessage,
            onTakePhotoClick = ::launchCamera
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoGalleryScreen(
    uiState: PhotoGalleryUiState,
    permissionMessage: String?,
    onTakePhotoClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Мои фото") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onTakePhotoClick) {
                Text(
                    text = "Фото",
                    style = MaterialTheme.typography.titleMedium
                )
            }
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
                    permissionMessage = permissionMessage,
                    onTakePhotoClick = onTakePhotoClick
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    permissionMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.photos,
                            key = { photo -> photo.name }
                        ) { photo ->
                            PhotoGridItem(photo = photo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGalleryScreen(
    modifier: Modifier = Modifier,
    permissionMessage: String?,
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
                text = "Сделайте первый снимок, и он появится в галерее",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            permissionMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(onClick = onTakePhotoClick) {
                Text(text = "Сделать первое фото")
            }
        }
    }
}

@Composable
private fun PhotoGridItem(photo: PhotoItem) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AsyncImage(
            model = photo.file,
            contentDescription = photo.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Text(
            text = photo.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

private fun File.toContentUri(context: android.content.Context): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        this
    )
}
