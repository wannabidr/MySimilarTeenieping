package com.sss.mysimilarteenieping.ui.main.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sss.mysimilarteenieping.R // R.string 등을 위해 필요
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.ui.main.MainUiState
import com.sss.mysimilarteenieping.ui.main.MainViewModel
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import com.sss.mysimilarteenieping.util.TestData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToSelectImage: () -> Unit,
    onNavigateToResult: (resultId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HistoryDrawerContent(
                historyState = uiState,
                onHistoryItemClick = {
                    onNavigateToResult(it.id)
                    scope.launch { drawerState.close() }
                },
                onRefresh = {
                    viewModel.loadHistory()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.app_name_main)) }, // strings.xml에 app_name_main 정의 필요
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(id = R.string.cd_open_history_drawer)) // cd_open_history_drawer 정의 필요
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(id = R.string.fab_start_analysis)) }, // fab_start_analysis 정의 필요
                    icon = { Icon(Icons.Filled.AddAPhoto, contentDescription = stringResource(id = R.string.cd_start_analysis)) }, // cd_start_analysis 정의 필요
                    onClick = onNavigateToSelectImage
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) {
            paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 초기 환영 메시지 또는 간단한 안내
                Text(
                    text = stringResource(id = R.string.welcome_message_main), // welcome_message_main 정의 필요
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Text(
                    text = stringResource(id = R.string.welcome_submessage_main), // welcome_submessage_main 정의 필요
                    style = MaterialTheme.typography.bodyLarge
                )

                // 에러 상태 표시
                if (uiState is MainUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error loading history: ${(uiState as MainUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MySimilarTeeniepingTheme {
        // Mock ViewModel or provide dummy state for preview
        val previewUiState = MainUiState.Success(TestData.previewHistoryList) // Use TestData
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { HistoryDrawerContent(historyState = previewUiState, onHistoryItemClick = {}, onRefresh = {}) }
        ) {
            Scaffold(
                topBar = { TopAppBar(title = { Text("나랑 닮은 티니핑은?") }) },
                floatingActionButton = { ExtendedFAB(onClick = {}) }
            ) {
                paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("나와 닮은 티니핑을 찾아보세요!", style = MaterialTheme.typography.headlineSmall)
                    Text("아래 버튼을 눌러 시작하세요.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun ExtendedFAB(onClick: () -> Unit) { // Preview용 간이 FAB
    ExtendedFloatingActionButton(
        onClick = onClick,
        text = { Text("테스트 시작") },
        icon = { Icon(Icons.Filled.AddAPhoto, "테스트 시작") }
    )
} 