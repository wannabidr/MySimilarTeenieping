package com.sss.mysimilarteenieping.ui.result.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Face
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sss.mysimilarteenieping.R // For placeholder, error drawables and strings
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.ui.result.ResultUiState
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import com.sss.mysimilarteenieping.util.TestData // Added import for TestData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onShoppingLinkClicked: (String) -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.result_screen_title)) }, // R.string.result_screen_title 정의 필요
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cd_navigate_back))
                    }
                },
                actions = {
                    if (uiState is ResultUiState.Success) {
                        IconButton(onClick = {
                            // TODO: 이미지 URI나 파일도 같이 공유하도록 개선 필요.
                            // 현재는 Teenieping 이름과 정확도만 공유.
                            val shareText = "나와 닮은 티니핑은 ${uiState.result.similarTeenieping.name}이래요! (정확도: ${String.format("%.0f", uiState.result.similarityScore * 100)}%)"
                            onShareClick(shareText)
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(id = R.string.cd_share_result)) // R.string.cd_share_result 정의 필요
                        }
                    }
                }
            )
        }
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ResultUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ResultUiState.Success -> {
                    ResultContent(result = uiState.result, onShoppingLinkClicked = onShoppingLinkClicked)
                }
                is ResultUiState.Error -> {
                    ErrorStateView(message = uiState.message, onRetryClick = onRetryClick)
                }
            }
        }
    }
}

@Composable
fun ResultContent(
    result: AnalysisResult,
    onShoppingLinkClicked: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(result.userImage.fbFilePath.ifEmpty { result.userImage.localFilePath })
                .crossfade(true)
                .build(),
            placeholder = rememberVectorPainter(image = Icons.Filled.Image),
            error = rememberVectorPainter(image = Icons.Filled.BrokenImage),
            contentDescription = stringResource(id = R.string.cd_user_image),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Teenieping Info
        Text(
            text = stringResource(id = R.string.result_your_teenieping_is), // R.string.result_your_teenieping_is 정의 필요
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = result.similarTeenieping.name,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "(정확도: ${String.format("%.0f", result.similarityScore * 100)}%)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(result.similarTeenieping.imagePath) // Firebase Storage URL or local/asset path
                .crossfade(true)
                .build(),
            placeholder = rememberVectorPainter(image = Icons.Filled.Face),
            error = rememberVectorPainter(image = Icons.Filled.BrokenImage),
            contentDescription = result.similarTeenieping.name,
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = result.similarTeenieping.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = "분석일시: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(result.analysisTimestamp))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Divider()

        // Shopping Links
        if (result.shoppingLinks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.result_shopping_links_title), // R.string.result_shopping_links_title 정의 필요
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            result.shoppingLinks.forEach {
                shoppingLink ->
                ShoppingLinkItem(link = shoppingLink, onClick = { onShoppingLinkClicked(shoppingLink.linkUrl) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ShoppingLinkItem(
    link: ShoppingLink,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Icon(
            Icons.Filled.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text = link.itemName.ifEmpty { "상품 보러가기" },
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.result_load_error_message, message), // R.string.result_load_error_message 정의 필요
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetryClick) {
            Text(stringResource(id = R.string.button_retry)) // R.string.button_retry 정의 필요
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
fun ResultScreenPreview_Success() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Success(
                result = TestData.resultScreenSuccessResult,
                shoppingLinks = TestData.resultScreenSuccessResult.shoppingLinks // Pass the shopping links from TestData
            ),
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview_Loading() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Loading, // Assuming ResultUiState.Loading is an object or class without params
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview_Error() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Error("Could not fetch result details for preview."), // Assuming ResultUiState.Error takes a String
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
} 