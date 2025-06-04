package com.sss.mysimilarteenieping.ui.main.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sss.mysimilarteenieping.R // R.string 등을 위해 필요
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.ui.main.MainUiState
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDrawerContent(
    historyState: MainUiState,
    onHistoryItemClick: (AnalysisResult) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.history_drawer_title), // R.string.history_drawer_title 정의 필요
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = stringResource(id = R.string.cd_refresh_history)) // cd_refresh_history 정의 필요
                }
            }
            Divider()

            when (historyState) {
                is MainUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MainUiState.Success -> {
                    if (historyState.history.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(id = R.string.history_empty)) // history_empty 정의 필요
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(historyState.history) {
                                result ->
                                HistoryItem(result = result, onClick = { onHistoryItemClick(result) })
                                Divider()
                            }
                        }
                    }
                }
                is MainUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.history_load_error, historyState.message), // history_load_error 정의 필요
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun HistoryDrawerContentPreview_Success() {
    MySimilarTeeniepingTheme {
        val dummyUserImage = UserImage("path", "fbPath", Date().time)
        val dummyTeenieping = TeeniepingInfo(name = "방글핑", description = "웃음이 많아요", imagePath = "")
        val history = listOf(
            AnalysisResult("1", dummyUserImage, dummyTeenieping, 0.9f, Date().time - 100000),
            AnalysisResult("2", dummyUserImage, dummyTeenieping.copy(name = "믿어핑"), 0.8f, Date().time)
        )
        HistoryDrawerContent(
            historyState = MainUiState.Success(history),
            onHistoryItemClick = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun HistoryDrawerContentPreview_Empty() {
    MySimilarTeeniepingTheme {
        HistoryDrawerContent(
            historyState = MainUiState.Success(emptyList()),
            onHistoryItemClick = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun HistoryDrawerContentPreview_Loading() {
    MySimilarTeeniepingTheme {
        HistoryDrawerContent(
            historyState = MainUiState.Loading,
            onHistoryItemClick = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun HistoryDrawerContentPreview_Error() {
    MySimilarTeeniepingTheme {
        HistoryDrawerContent(
            historyState = MainUiState.Error("Failed to load history"),
            onHistoryItemClick = {},
            onRefresh = {}
        )
    }
} 