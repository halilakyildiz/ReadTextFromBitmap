package com.example.readtextfrombitmap.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readtextfrombitmap.R
import com.example.readtextfrombitmap.viewmodel.OcrViewModel


@Composable
fun SettingsScreen(modifier: Modifier=Modifier,viewModel: OcrViewModel){
    var aboutusDialog by remember { mutableStateOf(false)}
    Column(modifier = modifier.fillMaxSize()
        .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)) {
        OcrDropDown(viewModel)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(4.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple() // Material3 uyumlu ripple
                ) {
                    aboutusDialog=true
                }
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 3.dp, end = 3.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Text(stringResource(R.string.about_us))
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = ""
                )
            }
        }
        if(aboutusDialog){
            AboutUsBottomSheet(
                onDismiss = {
                    aboutusDialog=false
                }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrDropDown(viewModel: OcrViewModel) {
    val options = listOf(
        stringResource(R.string.turkish),
        stringResource(R.string.english)
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }
    if(viewModel.getOcrTraniedData()=="tur")
        selectedText=options[0]
    else
        selectedText=options[1]

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.select_ocr_language)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        selectedText = item
                        viewModel.setOcrTraniedData(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_img),
                contentDescription = "App Icon"
            )
            Spacer(modifier = Modifier.padding(5.dp))
            Text(stringResource(R.string.about_us_app_info),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center)


            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement =Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.about_us_contact_us),
                    style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.padding(5.dp))
                val email = stringResource(R.string.about_us_contact_adress)
                val context = LocalContext.current
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple() // Material3 uyumlu ripple
                    ) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$email")
                        }
                        context.startActivity(intent)
                    })
            }
            Spacer(modifier = Modifier.padding(5.dp))
            Text("Version 1.0",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                textAlign = TextAlign.Center)
            Text(stringResource(R.string.about_us_developer_info),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                textAlign = TextAlign.Center)
        }
    }
}