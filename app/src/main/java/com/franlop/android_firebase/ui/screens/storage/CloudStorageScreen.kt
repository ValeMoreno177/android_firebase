package com.franlop.android_firebase.ui.screens.storage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.franlop.android_firebase.utils.CloudStorageManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudStorageScreen(storage: CloudStorageManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gallery by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploading by remember { mutableStateOf(false) }

    // Archivo y Uri donde la cámara guardará la foto
    var photoFile by remember { mutableStateOf<File?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    suspend fun refresh() {
        gallery = try {
            storage.getUserImages()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar fotos: ${e.message}", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    LaunchedEffect(Unit) { refresh() }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = photoFile
        val uri = photoUri
        if (success && file != null && uri != null) {
            Toast.makeText(context, "Foto tomada", Toast.LENGTH_SHORT).show()
            scope.launch {
                uploading = true
                try {
                    storage.uploadFile(file.name, uri)
                    refresh()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al subir: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                uploading = false
            }
        } else {
            Toast.makeText(context, "No se tomó la foto", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera() {
        val file = context.createImageFile()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        photoFile = file
        photoUri = uri
        cameraLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    launchCamera()
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tomar foto")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (gallery.isEmpty() && !uploading) {
                Text(
                    text = "No tienes fotos.\nPresiona + para tomar una.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize()
            ) {
                items(gallery) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            if (uploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", externalCacheDir)
}
