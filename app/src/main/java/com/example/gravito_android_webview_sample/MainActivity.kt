package com.example.gravito_android_webview_sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.gravito_android_webview_sample.ui.theme.GravitoandroidwebviewsampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GravitoandroidwebviewsampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(text = "Hello $name!")
        Button(onClick = {
            // Navigate to WebViewActivity
            val intent = Intent(context, WebViewActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Open WebView")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GravitoandroidwebviewsampleTheme {
        Greeting("Android")
    }
}