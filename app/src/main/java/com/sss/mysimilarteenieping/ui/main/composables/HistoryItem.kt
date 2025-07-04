package com.sss.mysimilarteenieping.ui.main.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryItem(
    result: AnalysisResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(result.userImage.fbFilePath.ifEmpty { result.userImage.localFilePath })
                    .crossfade(true)
                    .build(),
                placeholder = rememberVectorPainter(image = Icons.Filled.Image),
                error = rememberVectorPainter(image = Icons.Filled.BrokenImage),
                contentDescription = "User analyzed image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.similarTeenieping.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Similarity: ${String.format("%.0f", result.similarityScore * 100)}%",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(result.analysisTimestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryItemPreview() {
    MySimilarTeeniepingTheme {
        val dummyUserImage = UserImage(
            localFilePath = "", 
            fbFilePath = "https://via.placeholder.com/150",
            createdAt = Date().time
        )
        val dummyTeenieping = TeeniepingInfo(
            id = 0,
            name = "방글핑",
            description = "항상 방긋 웃는 귀여운 티니핑",
            imagePath = "dummy_teenieping_image.png"
        )
        val dummyResult = AnalysisResult(
            id = "res001",
            userImage = dummyUserImage,
            similarTeenieping = dummyTeenieping,
            similarityScore = 0.87f,
            analysisTimestamp = Date().time
        )
        HistoryItem(result = dummyResult, onClick = {})
    }
} 