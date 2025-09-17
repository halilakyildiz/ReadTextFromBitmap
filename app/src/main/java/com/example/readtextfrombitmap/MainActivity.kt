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
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.readtextfrombitmap.Utils.hasCamera
import com.example.readtextfrombitmap.viewmodel.OcrViewModel
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.readtextfrombitmap.model.OcrResults
import com.example.readtextfrombitmap.screens.LoadingAnimation
import com.example.readtextfrombitmap.screens.Screen
import com.example.readtextfrombitmap.ui.theme.*
import java.io.File


class MainActivity : ComponentActivity() {
    val viewModel:OcrViewModel by viewModels<OcrViewModel>()
    /*
    val viewModel: OcrViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(application)
    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadTextFromBitmapTheme {
                MainScreen(viewModel=viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier,viewModel:OcrViewModel){
    val navItemList = listOf(
        NavItem("History", History),
        NavItem("New", Icons.Default.Add),
        NavItem("Settings", Icons.Default.Settings)
    )

    var selectedIndex by remember {
        mutableStateOf(1)
    }
    Scaffold(modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed{index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex=index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                        },
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
        }) { innerPadding ->
        ContentScreen(modifier = modifier.padding(innerPadding),selectedIndex,viewModel)
    }
}
@Composable
fun ContentScreen(modifier: Modifier = Modifier,selectedIndex:Int,viewModel:OcrViewModel){
    when(selectedIndex){
        0->HistoryScreen(modifier,viewModel)
        1-> Screen(modifier,viewModel)
        2-> SettingsScreen(modifier)
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier){
    Box(modifier =modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){
        Text("Settings Screen")
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier,viewModel:OcrViewModel){
    val results by viewModel.results.collectAsState()
    val loading = viewModel.isLoading

    if (loading) {
        LoadingAnimation()
    } else {
        if(results.size>0)
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                items(results) { ocr_result ->
                    OcrResultCard(ocr_result)
                }
            }
        else
            Box(modifier=modifier.fillMaxSize(),
                contentAlignment = Alignment.Center){
                Text("No Data")
            }
    }
}
@Composable
fun OcrResultCard(
    ocrResult:OcrResults
) {
    Card(
        modifier = Modifier
            .size(width = 80.dp,height = 120.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background  // Light/Dark otomatik
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ocrResult.img_ocr_result != null) {
                println("img url -> ${ocrResult.img}")
                val path = ocrResult.img
                val file = File(path)
                val uri = Uri.fromFile(file)
                val painter = rememberAsyncImagePainter(
                    model = path,
                    placeholder = painterResource(R.drawable.no_img),
                    error = painterResource(R.drawable.no_img)
                )
                uri?.let{
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }?:run{
                    Box(modifier=Modifier.weight(1f),
                        contentAlignment = Alignment.Center){
                        Text("No Image", fontSize = 10.sp)
                    }
                }
            }
            else{
                Box(modifier=Modifier.weight(1f),
                    contentAlignment = Alignment.Center){
                    Text("No Image", fontSize = 10.sp)
                }
            }
            HorizontalDivider()
            Text(
                text = ocrResult.ocr_time,
                modifier=Modifier.padding(top = 3.dp, bottom = 3.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 8.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReadTextFromBitmapTheme {

    }
}