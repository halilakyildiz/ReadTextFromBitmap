package com.example.readtextfrombitmap.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readtextfrombitmap.R
import com.example.readtextfrombitmap.viewmodel.OcrViewModel

@Composable
fun SettingsScreen(modifier: Modifier=Modifier,viewModel: OcrViewModel){
    Column(modifier = modifier.fillMaxSize()
        .padding(5.dp)) {
        OcrDropDown(viewModel)
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