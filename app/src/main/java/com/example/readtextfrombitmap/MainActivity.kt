package com.example.readtextfrombitmap

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.readtextfrombitmap.ui.theme.ReadTextFromBitmapTheme
import java.io.File
import coil.compose.rememberAsyncImagePainter
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadTextFromBitmapTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()){
                        Screen()
                    }

                }
            }
        }
    }
}

@Composable
fun Screen(){
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    //var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraAvailable = hasCamera(context)
    var bitmapText by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(context.cacheDir, "temp_image.jpg")
        )
    }
    LaunchedEffect(imageUri) {
        imageUri?.let {
            isProcessing=true
            withContext(Dispatchers.IO){
                val bitmap = uriToBitmap(context, it)
                bitmap?.let{
                    bitmapText = recognizeText(bitmap!!,context)
                    isProcessing=false
                }
            }
        }
    }

    if (isProcessing) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
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

    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                galleryLauncher.launch("image/*")
            }) {
                Text("From Gallery")
            }
            Button(onClick = {
                if(cameraAvailable)
                    cameraLauncher.launch(photoUri)
                else
                    Toast.makeText(context,"The device has not camera",Toast.LENGTH_SHORT).show()
            }) {
                Text("From Camera")
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .width(1.dp),
            color = Color.Gray
        )
        Box(
            modifier = Modifier.padding(start = 5.dp, end = 5.dp)
        ){
            Column {
                Text("Selected Image",
                    modifier = Modifier.fillMaxWidth())
                var showDialog by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showDialog = true } // tıklayınca büyüt
                        )
                    } else {
                        Text("No Image")
                    }
                }
                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
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
                                    .clickable { showDialog = false } // kapatmak için tıkla
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text("Text from Image",
                    modifier = Modifier.fillMaxWidth())
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .border(1.dp,MaterialTheme.colorScheme.secondary)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center // hem yatay hem dikey ortala,
                ) {
                    bitmapText?.let{
                        TextField(
                            value = it,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = Color.Black),
                            singleLine = false,
                            keyboardOptions = KeyboardOptions.Default,
                            keyboardActions = KeyboardActions.Default,
                            maxLines = Int.MAX_VALUE
                        )
                    }?:run{
                        Text("No text")
                    }

                }
            }
        }
    }
}
fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    val bitmap = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
    return bitmap.copy(Bitmap.Config.ARGB_8888, true)
}
fun copyAssetIfNeeded(context: Context, resId: Int, outDir: File,outFileName:String) {
    val outFile = File(outDir, outFileName)
    if (!outFile.exists()) {
        outDir.mkdirs()
        try {
            context.resources.openRawResource(resId).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            println("Dosya başarıyla kopyalandı: ${outFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Dosya kopyalanamadı!")
        }
    } else {
        println("Dosya zaten var: ${outFile.absolutePath}")
    }
}
fun recognizeText(bitmap: Bitmap, context: Context): String {
    try{
        val tess = TessBaseAPI()
        val dataPath = context.filesDir.absolutePath + "/tesseract/"
        val tessDataDir = File(dataPath, "tessdata")
        println(dataPath)
        // raw içindeki tur.traineddata dosyasını kopyala
        copyAssetIfNeeded(context, R.raw.tur, tessDataDir, "tur.traineddata")
        tess.init(dataPath, "tur")
        println("Bitmap boyutu: ${bitmap.width}x${bitmap.height}")
        tess.setImage(bitmap)
        val extractedText = tess.utF8Text
        tess.end()
        return extractedText
    }catch (e:Exception){
        println(e.message)
        return e.message ?: "Hata !"
    }
}
fun hasCamera(context: Context): Boolean {
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReadTextFromBitmapTheme {
        Screen()
    }
}