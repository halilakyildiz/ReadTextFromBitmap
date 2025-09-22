package com.example.readtextfrombitmap.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.readtextfrombitmap.R
import com.example.readtextfrombitmap.Utils.hasCamera
import com.example.readtextfrombitmap.model.OcrResults
import com.example.readtextfrombitmap.ui.theme.Camera_enhance
import com.example.readtextfrombitmap.ui.theme.Gallery_thumbnail
import com.example.readtextfrombitmap.viewmodel.OcrViewModel
import java.io.File

@Composable
fun OcrScreen(modifier: Modifier = Modifier, viewModel: OcrViewModel){
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerSheet by remember { mutableStateOf(false) }
    val cameraAvailable = hasCamera(context)
    var showLoadingDialog by remember { mutableStateOf(false) }

    val bitmapText = viewModel.text
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(context.cacheDir, "temp_image.jpg")
        )
    }

    LaunchedEffect(imageUri) {
        imageUri?.let {
            showLoadingDialog=true
            viewModel.processBitmap(it)
            showLoadingDialog=false
        }
    }

    if (showLoadingDialog) {
        LoadingAnimation()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (!success) return@rememberLauncherForActivityResult
        else{
            imageUri = photoUri
        }
    }

    // Content çağırılır
    OcrContent(
        bitmapText = bitmapText,
        imageUri = imageUri,
        imagePicker = {
            showImagePickerSheet=true
        },
        modifier = modifier
    )
    // Bottom Sheet
    if (showImagePickerSheet) {
        ImagePickerSheet(
            onDismiss = { showImagePickerSheet = false },
            onGalleryClick = {
                showImagePickerSheet = false
                galleryLauncher.launch("image/*")
            },
            onCameraClick = {
                if(cameraAvailable)
                    cameraLauncher.launch(photoUri!!)
                else {
                    Toast.makeText(context,"The device has not camera", Toast.LENGTH_SHORT).show()
                    showImagePickerSheet=false
                }
            }
        )
    }
}
@Composable
fun OcrContent(
    bitmapText: String="",
    modifier: Modifier=Modifier,
    imageUri: Uri?,
    imagePicker:(()-> Unit)?=null
    ){
    Column(
        modifier = modifier
            .padding(5.dp)
    ) {
        var showImageDialog by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable {
                    if (imageUri == null)
                        imagePicker?.invoke()
                    else showImageDialog = true
                },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(stringResource(R.string.add_new), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        // Image Preview Dialog
        if (showImageDialog) {
            Dialog(onDismissRequest = { showImageDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showImageDialog = false } // kapatmak için tıkla
                    )
                }
            }
        }

        // OCR Text Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            tonalElevation = 2.dp, // hafif gölge için (opsiyonel)
            color = MaterialTheme.colorScheme.surface // arka plan tema uyumlu
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                if(bitmapText!="") {
                    TextField(
                        value = bitmapText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        ),
                        singleLine = false,
                        keyboardOptions = KeyboardOptions.Default,
                        keyboardActions = KeyboardActions.Default,
                        maxLines = Int.MAX_VALUE
                    )
                }
                else{
                    Text(
                        text = stringResource(R.string.ocr_result_not_text),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
@Composable
fun LoadingAnimation(){
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.from_gallery)) },
                leadingContent = { Icon(Gallery_thumbnail, contentDescription = null) },
                modifier = Modifier.clickable { onGalleryClick() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.from_camera)) },
                leadingContent = { Icon(Camera_enhance, contentDescription = null) },
                modifier = Modifier.clickable { onCameraClick() }
            )
        }
    }
}