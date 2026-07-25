package io.github.lucasfaiska.kmpdf.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfViewer

private const val SAMPLE_URL = "https://raw.githubusercontent.com/mozilla/pdf.js/ba2edeae/web/compressed.tracemonkey-pldi-09.pdf"
private const val BUTTON_WIDTH_FRACTION = 0.7f

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
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let { onSourceSelected(PdfSource.Local(it.toString())) }
        }

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
                    PdfSource.Url(SAMPLE_URL),
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                    .padding(8.dp),
        ) {
            Text("Load from URL (Remote)")
        }

        Button(
            onClick = {
                onSourceSelected(
                    PdfSource.Local("file:///android_asset/sample.pdf"),
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                    .padding(8.dp),
        ) {
            Text("Load from Assets (Local)")
        }

        Button(
            onClick = { launcher.launch(arrayOf("application/pdf")) },
            modifier =
                Modifier
                    .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                    .padding(8.dp),
        ) {
            Text("Load from Device (Local)")
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
        val modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)

        when (source) {
            is PdfSource.Url ->
                PdfViewer(
                    url = source.url,
                    modifier = modifier,
                    showToolbar = true,
                )

            is PdfSource.Local ->
                PdfViewer(
                    identifier = source.identifier,
                    modifier = modifier,
                    showToolbar = true,
                )
        }
    }
}
