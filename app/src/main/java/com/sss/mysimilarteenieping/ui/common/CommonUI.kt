package com.sss.mysimilarteenieping.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sss.mysimilarteenieping.ui.theme.PastelPink
import com.sss.mysimilarteenieping.ui.theme.PastelPurple
import com.sss.mysimilarteenieping.ui.theme.PastelSkyBlue
import com.sss.mysimilarteenieping.ui.theme.TextColor

@Composable
fun GradientBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PastelPink.copy(alpha = 0.7f),
                        PastelSkyBlue.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun CuteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = TextColor,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium, spotColor = PastelPurple)
            .padding(4.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        content()
    }
} 