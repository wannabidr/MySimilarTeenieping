package com.sss.mysimilarteenieping.ui.select.composables

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sss.mysimilarteenieping.R // For R.string.*
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.select_image_screen_title)) }, // R.string.select_image_screen_title 정의 필요
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cd_navigate_back)) // R.string.cd_navigate_back 정의 필요
                    }
                }
            )
        }
    ) {
        paddingValues ->
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState is SelectImageUiState.ImageSelected) {
                    Image(
                        bitmap = uiState.imageBitmap.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.cd_selected_image_preview), // R.string.cd_selected_image_preview 정의 필요
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(stringResource(id = R.string.select_image_placeholder)) // R.string.select_image_placeholder 정의 필요
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onGalleryClick) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.button_gallery)) // R.string.button_gallery 정의 필요
                }
                Button(onClick = onCameraClick) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.button_camera)) // R.string.button_camera 정의 필요
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is SelectImageUiState.Idle, is SelectImageUiState.ImageSelected -> {
                    Button(
                        onClick = onAnalyzeClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState is SelectImageUiState.ImageSelected
                    ) {
                        Text(stringResource(id = R.string.button_analyze)) // R.string.button_analyze 정의 필요
                    }
                }
                is SelectImageUiState.Analyzing -> {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                    Text(stringResource(id = R.string.analyzing_in_progress)) // R.string.analyzing_in_progress 정의 필요
                }
                is SelectImageUiState.AnalysisError -> {
                    Text(
                        text = stringResource(id = R.string.analysis_error_message, uiState.message), // R.string.analysis_error_message 정의 필요
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = onAnalyzeClick, // Allow retry
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentImageSelected(uiState) // Enable if an image was previously selected
                    ) {
                        Text(stringResource(id = R.string.button_retry_analysis)) // R.string.button_retry_analysis 정의 필요
                    }
                }
                is SelectImageUiState.AnalysisSuccess -> {
                    // Navigated away, usually no UI shown here for success on this screen
                    Text(stringResource(id = R.string.analysis_success_navigating)) // R.string.analysis_success_navigating 정의 필요
                }
            }
        }
    }
}

// Helper to check if an image is selected, for retrying after error
private fun currentImageSelected(uiState: SelectImageUiState): Boolean {
    // This is a bit of a workaround. Ideally, the ViewModel would retain the selected image info
    // even if an error occurs, and the UI state would reflect that.
    // For simplicity here, we assume if it's an error state, there's no selected image info in the state itself.
    // A more robust solution would be to have uiState.ImageSelected carry the image even during error, or viewModel to expose it separately.
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
        // Create a dummy bitmap for preview
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