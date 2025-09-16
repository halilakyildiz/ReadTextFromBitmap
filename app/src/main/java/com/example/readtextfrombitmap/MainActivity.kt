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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import java.io.File
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
import com.example.readtextfrombitmap.screens.Screen
import com.example.readtextfrombitmap.ui.theme.*


class MainActivity : ComponentActivity() {
    val viewModel:OcrViewModel by viewModels<OcrViewModel>()

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
        0->HistoryScreen(modifier)
        1-> Screen(modifier,viewModel)
        2-> SettingsScreen(modifier)
    }
}
@Composable
fun HistoryScreen(modifier: Modifier = Modifier){
    Box(modifier =modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){
        Text("History Screen")
    }
}
@Composable
fun SettingsScreen(modifier: Modifier = Modifier){
    Box(modifier =modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){
        Text("Settings Screen")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReadTextFromBitmapTheme {
         HistoryScreen()
    }
}