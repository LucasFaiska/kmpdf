package io.github.lucasfaiska.kmpdf.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfViewer

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SampleApp()
                }
            }
        }
    }
}

@Composable
fun SampleApp() {
    var selectedSource by remember { mutableStateOf<PdfSource?>(null) }

    if (selectedSource == null) {
        SelectionScreen(onSourceSelected = { selectedSource = it })
    } else {
        ViewerScreen(
            source = selectedSource!!,
            onBack = { selectedSource = null },
        )
    }
}

@Composable
fun SelectionScreen(onSourceSelected: (PdfSource) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "kmPDF Sample App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Button(
            onClick = {
                onSourceSelected(
                    PdfSource.Url("https://raw.githubusercontent.com/mozilla/pdf.js/ba2edeae/web/compressed.tracemonkey-pldi-09.pdf"),
                )
            },
            modifier = Modifier.fillMaxWidth(0.7f).padding(8.dp),
        ) {
            Text("Load from URL (Remote)")
        }

        Button(
            onClick = {
                onSourceSelected(
                    PdfSource.Local("file:///android_asset/sample.pdf"),
                )
            },
            modifier = Modifier.fillMaxWidth(0.7f).padding(8.dp),
        ) {
            Text("Load from Assets (Local)")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ViewerScreen(
    source: PdfSource,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (source is PdfSource.Url) "Remote PDF" else "Local PDF") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { padding ->
        PdfViewer(
            source = source,
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}
