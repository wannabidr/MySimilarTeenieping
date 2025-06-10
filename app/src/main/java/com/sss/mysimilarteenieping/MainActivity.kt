package com.sss.mysimilarteenieping

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sss.mysimilarteenieping.ui.main.composables.MainScreen 
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySimilarTeeniepingTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(
                        onNavigateToSelectImage = {
                            startActivity(Intent(this@MainActivity, SelectImageActivity::class.java))
                        },
                        onNavigateToResult = { resultId ->
                            val intent = Intent(this@MainActivity, ResultActivity::class.java)
                            intent.putExtra("resultId", resultId)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MySimilarTeeniepingTheme {
        MainScreen(onNavigateToSelectImage = {}, onNavigateToResult = {})
    }
}