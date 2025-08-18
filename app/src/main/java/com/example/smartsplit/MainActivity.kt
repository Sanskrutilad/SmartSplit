package com.example.smartsplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartsplit.ui.theme.SmartSplitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartSplitTheme {
                LaunchAnimationAppName()
            }
        }
    }
}

@Composable
fun LaunchAnimationAppName() {
    val appName = "SmartSplit"
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            appName.forEachIndexed { index, char ->
                var visible by remember { mutableStateOf(false) }
                val offsetY = remember { Animatable(-500f) } // start far above screen
                val alpha = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    delay(index * 150L)
                    visible = true
                    scope.launch {
                        offsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 800,
                                easing = BounceInterpolatorEasing
                            )
                        )
                    }
                    scope.launch {
                        alpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 400)
                        )
                    }
                }

                if (visible) {
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.graphicsLayer {
                            translationY = offsetY.value
                            this.alpha = alpha.value
                        }
                    )
                }
            }
        }
    }
}

val BounceInterpolatorEasing = Easing { fraction ->

    val t = fraction
    when {
        t < 0.3535f -> (7.5625f * t * t)
        t < 0.7408f -> {
            val t2 = t - 0.54719f
            (7.5625f * t2 * t2 + 0.75f)
        }
        t < 0.9644f -> {
            val t2 = t - 0.8526f
            (7.5625f * t2 * t2 + 0.9375f)
        }
        else -> {
            val t2 = t - 0.9544f
            (7.5625f * t2 * t2 + 0.984375f)
        }
    }
}
