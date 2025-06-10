package com.sss.mysimilarteenieping.ui.result.composables

import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sss.mysimilarteenieping.R
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.ui.common.GradientBox
import com.sss.mysimilarteenieping.ui.result.ResultUiState
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import com.sss.mysimilarteenieping.util.TestData 
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import com.sss.mysimilarteenieping.ui.result.ResultViewModel
import com.sss.mysimilarteenieping.BuildConfig
import java.io.IOException
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    uiState: ResultUiState,
    shoppingLinks: List<ShoppingLink>,
    isShoppingLoading: Boolean,
    onShoppingLinkClicked: (String) -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: (String) -> Unit
) {
    GradientBox {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.result_screen_title)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cd_navigate_back))
                        }
                    },
                    actions = {
                        if (uiState is ResultUiState.Success) {
                            IconButton(onClick = {
                                val shareText = "나와 닮은 티니핑은 ${uiState.result.similarTeenieping.name}이래요! (정확도: ${String.format("%.0f", uiState.result.similarityScore * 100)}%)"
                                onShareClick(shareText)
                            }) {
                                Icon(Icons.Filled.Share, contentDescription = stringResource(id = R.string.cd_share_result))
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
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
                        ResultContent(
                            result = uiState.result,
                            shoppingLinks = shoppingLinks,
                            isShoppingLoading = isShoppingLoading,
                            onShoppingLinkClicked = onShoppingLinkClicked
                        )
                    }
                    is ResultUiState.Error -> {
                        ErrorStateView(message = uiState.message, onRetryClick = onRetryClick)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultContent(
    result: AnalysisResult,
    shoppingLinks: List<ShoppingLink>,
    isShoppingLoading: Boolean,
    onShoppingLinkClicked: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            ImageCard(
                imageUrl = result.userImage.fbFilePath.ifEmpty { result.userImage.localFilePath },
                label = "내 사진"
            )
            Text("💖", fontSize = 32.sp)
            ImageCard(
                imageUrl = "file:///android_asset/${result.similarTeenieping.imagePath.normalizeToNFC()}",
                label = result.similarTeenieping.name
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "✨짜잔! 당신과 닮은 티니핑은...✨",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = result.similarTeenieping.name,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "(정확도: ${String.format("%.0f", result.similarityScore * 100)}%)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = result.chatGptDescription ?: result.similarTeenieping.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        result.similarTeenieping.details?.let { details ->
            if (details.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "✨ 당신만을 위한 특별한 설명",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = details,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.result_shopping_links_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (isShoppingLoading) {
            ShoppingLinksLoadingSkeletons()
        } else if (shoppingLinks.isNotEmpty()) {
            shoppingLinks.forEach { shoppingLink ->
                ShoppingLinkItem(
                    link = shoppingLink,
                    onClick = { onShoppingLinkClicked(shoppingLink.linkUrl) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(
                text = stringResource(id = R.string.result_no_shopping_links_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun ImageCard(imageUrl: String, label: String) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                placeholder = rememberVectorPainter(image = Icons.Filled.Face),
                error = rememberVectorPainter(image = Icons.Filled.BrokenImage),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(MaterialTheme.shapes.large)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShoppingLinksLoadingSkeletons(count: Int = 2) {
    Column {
        repeat(count) {
            ShoppingLinkItemSkeleton()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ShoppingLinkItemSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) 
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
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
            text = stringResource(id = R.string.result_load_error_message, message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetryClick) {
            Text(stringResource(id = R.string.button_retry))
        }
    }
}

/**
 * 주어진 문자열을 NFC(완성형)으로 정규화합니다.
 */
fun String.normalizeToNFC(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFC)
}

/**
 * 주어진 문자열을 NFD(조합형)으로 정규화합니다.
 */
fun String.normalizeToNFD(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
}

/**
 * 두 문자열이 한글 정규화 형태를 무시하고 논리적으로 동등한지 확인합니다.
 * 일반적으로 NFC(완성형)으로 정규화하여 비교하는 것이 가장 일반적입니다.
 */
fun String.isHangulEquivalent(other: String): Boolean {
    return this.normalizeToNFC() == other.normalizeToNFC()
}

@Preview(showBackground = true, name = "Result Screen Success")
@Composable
fun ResultScreenPreview_Success() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Success(
                result = TestData.resultScreenSuccessResult
            ),
            shoppingLinks = TestData.previewShoppingLinks,
            isShoppingLoading = false,
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Result Screen Shopping Loading")
@Composable
fun ResultScreenPreview_ShoppingLoading() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Success(
                result = TestData.resultScreenSuccessResult
            ),
            shoppingLinks = emptyList(),
            isShoppingLoading = true, 
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Result Screen No Shopping Links")
@Composable
fun ResultScreenPreview_NoShoppingLinks() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Success(
                result = TestData.resultScreenSuccessResult
            ),
            shoppingLinks = emptyList(),
            isShoppingLoading = false,
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Result Screen Error")
@Composable
fun ResultScreenPreview_Error() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Error("미리보기 에러 메시지"),
            shoppingLinks = emptyList(),
            isShoppingLoading = false,
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Result Screen Loading")
@Composable
fun ResultScreenPreview_Loading() {
    MySimilarTeeniepingTheme {
        ResultScreen(
            uiState = ResultUiState.Loading,
            shoppingLinks = emptyList(),
            isShoppingLoading = false,
            onShoppingLinkClicked = {},
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {}
        )
    }
} 