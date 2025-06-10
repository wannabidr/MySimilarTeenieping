package com.sss.mysimilarteenieping.ui.select.composables

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sss.mysimilarteenieping.R
import com.sss.mysimilarteenieping.ui.common.CuteButton
import com.sss.mysimilarteenieping.ui.common.GradientBox
import com.sss.mysimilarteenieping.ui.select.SelectImageUiState
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectImageScreen(
    uiState: SelectImageUiState,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onBackClick: () -> Unit
) {
    GradientBox {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.select_image_screen_title)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onBackClick, enabled = uiState !is SelectImageUiState.Analyzing) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cd_navigate_back))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState is SelectImageUiState.ImageSelected) {
                        Image(
                            bitmap = uiState.imageBitmap.asImageBitmap(),
                            contentDescription = stringResource(id = R.string.cd_selected_image_preview),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.select_image_placeholder),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CuteButton(onClick = onGalleryClick, enabled = uiState !is SelectImageUiState.Analyzing) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.button_gallery))
                    }
                    CuteButton(onClick = onCameraClick, enabled = uiState !is SelectImageUiState.Analyzing) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.button_camera))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (uiState) {
                    is SelectImageUiState.Idle, is SelectImageUiState.ImageSelected -> {
                        CuteButton(
                            onClick = onAnalyzeClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState is SelectImageUiState.ImageSelected,
                            backgroundColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(stringResource(id = R.string.button_analyze))
                        }
                    }
                    is SelectImageUiState.Analyzing -> {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                        Text(stringResource(id = R.string.analyzing_in_progress))
                    }
                    is SelectImageUiState.AnalysisError -> {
                        Text(
                            text = stringResource(id = R.string.analysis_error_message, uiState.message),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        CuteButton(
                            onClick = onAnalyzeClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentImageSelected(uiState)
                        ) {
                            Text(stringResource(id = R.string.button_retry_analysis))
                        }
                    }
                    is SelectImageUiState.AnalysisSuccess -> {
                        Text(stringResource(id = R.string.analysis_success_navigating))
                    }
                }
            }
        }
    }
}

private fun currentImageSelected(uiState: SelectImageUiState): Boolean {
    return uiState !is SelectImageUiState.Idle && uiState !is SelectImageUiState.Analyzing
}

@Preview(showBackground = true)
@Composable
fun SelectImageScreenPreview_Idle() {
    MySimilarTeeniepingTheme {
        SelectImageScreen(
            uiState = SelectImageUiState.Idle,
            onGalleryClick = {}, onCameraClick = {}, onAnalyzeClick = {}, onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectImageScreenPreview_ImageSelected() {
    MySimilarTeeniepingTheme {
        val context = LocalContext.current
        val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.LTGRAY)
        }
        SelectImageScreen(
            uiState = SelectImageUiState.ImageSelected(Uri.EMPTY, dummyBitmap),
            onGalleryClick = {}, onCameraClick = {}, onAnalyzeClick = {}, onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectImageScreenPreview_Analyzing() {
    MySimilarTeeniepingTheme {
        SelectImageScreen(
            uiState = SelectImageUiState.Analyzing,
            onGalleryClick = {}, onCameraClick = {}, onAnalyzeClick = {}, onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectImageScreenPreview_Error() {
    MySimilarTeeniepingTheme {
        SelectImageScreen(
            uiState = SelectImageUiState.AnalysisError("Failed to detect face."),
            onGalleryClick = {}, onCameraClick = {}, onAnalyzeClick = {}, onBackClick = {}
        )
    }
} 