package com.example.readtextfrombitmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.readtextfrombitmap.viewmodel.OcrViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.readtextfrombitmap.screens.HistoryScreen
import com.example.readtextfrombitmap.screens.OcrScreen
import com.example.readtextfrombitmap.screens.SettingsScreen
import com.example.readtextfrombitmap.ui.theme.*

class MainActivity : ComponentActivity() {
    val viewModel:OcrViewModel by viewModels<OcrViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            ReadTextFromBitmapTheme {
                val navItemList = listOf(
                    NavItem(stringResource(R.string.history), History),
                    NavItem(stringResource(R.string.new_add), Icons.Default.Add),
                    NavItem(stringResource(R.string.settings), Icons.Default.Settings)
                )
                var selectedIndex by remember {
                    mutableStateOf(1)
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            navItemList.forEachIndexed{index, navItem ->
                                NavigationBarItem(
                                    selected = selectedIndex == index,
                                    onClick = {
                                        selectedIndex=index
                                        when(index){
                                            0->navController.navigate("history_screen")
                                            1->navController.navigate("ocr_screen")
                                            2->navController.navigate("settings_screen")
                                        }
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
                        NavHost(
                            navController = navController,
                            startDestination = "ocr_screen"
                        ) {
                            composable("ocr_screen") { OcrScreen(viewModel=viewModel, modifier = Modifier.padding(innerPadding)) }
                            composable("history_screen") { HistoryScreen(viewModel=viewModel, modifier = Modifier.padding(innerPadding)) }
                            composable("settings_screen") { SettingsScreen(modifier = Modifier.padding(innerPadding),viewModel=viewModel) }
                        }
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReadTextFromBitmapTheme {

    }
}