package com.example.readtextfrombitmap.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.readtextfrombitmap.R
import com.example.readtextfrombitmap.model.OcrResults
import com.example.readtextfrombitmap.viewmodel.OcrViewModel

@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: OcrViewModel){
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
                    OcrResultCard(ocr_result,viewModel)
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
fun MyQuestionDialog(
    title:String="Warning",
    message:String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun OcrResultCard(
    ocrResult: OcrResults,
    viewModel: OcrViewModel
) {
    var showOcrDialog by remember  { mutableStateOf(false)}
    val path = ocrResult.img
    val painter = rememberAsyncImagePainter(
        model = path,
        placeholder = painterResource(R.drawable.no_img),
        error = painterResource(R.drawable.no_img)
    )
    Card(
        modifier = Modifier
            .size(width = 80.dp,height = 120.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background  // Light/Dark otomatik
        ),
        onClick = {
            showOcrDialog=true
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            HorizontalDivider()
            Text(
                text = ocrResult.ocr_time,
                modifier= Modifier.padding(top = 3.dp, bottom = 3.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 8.sp
                )
            )
        }
    }
    if(showOcrDialog){
        Dialog(onDismissRequest = { showOcrDialog = false }) {
            var aletDialog by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(ocrResult.ocr_time,
                        style = MaterialTheme.typography.bodySmall)
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete this ocr result",
                        modifier = Modifier.clickable {
                            //delete
                            aletDialog=true
                        },
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                HorizontalDivider(modifier = Modifier.padding(top = 3.dp, bottom = 3.dp))
                OcrContent(imageUri = path?.toUri(), bitmapText = ocrResult.img_ocr_result)
            }
            if(aletDialog){
                MyQuestionDialog(
                    message = "Are you sure delete this OCR ?",
                    onDismiss = {aletDialog=false},
                    onConfirm = {
                        viewModel.deleteOcr(ocrResult)
                        showOcrDialog=false
                    }
                )
            }
        }
    }
}