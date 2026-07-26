package io.github.lucasfaiska.kmpdf.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.androidApp.ui.CustomViewerScreen
import io.github.lucasfaiska.kmpdf.material3.Material3LoadingContent
import io.github.lucasfaiska.kmpdf.material3.Material3PasswordDialog
import io.github.lucasfaiska.kmpdf.material3.Material3PdfToolbar
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfViewer
import io.github.lucasfaiska.kmpdf.ui.rememberPdfRepository
import io.github.lucasfaiska.kmpdf.ui.rememberPdfViewerState

private const val SAMPLE_URL =
    "https://raw.githubusercontent.com/mozilla/pdf.js/ba2edeae/web/compressed.tracemonkey-pldi-09.pdf"
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
    var useCustomUi by remember { mutableStateOf(false) }

    if (selectedSource == null) {
        SelectionScreen(
            onSourceSelected = { source, isCustom ->
                selectedSource = source
                useCustomUi = isCustom
            },
        )
    } else {
        if (useCustomUi) {
            CustomViewerScreen(
                source = selectedSource!!,
                onBack = { selectedSource = null },
            )
        } else {
            ViewerScreen(
                source = selectedSource!!,
                onBack = { selectedSource = null },
            )
        }
    }
}

@Composable
fun SelectionScreen(onSourceSelected: (PdfSource, Boolean) -> Unit) {
    var isCustomMode by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let { onSourceSelected(PdfSource.Local(it.toString()), isCustomMode) }
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "kmPDF Sample App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        ModeToggle(
            isCustomMode = isCustomMode,
            onModeChanged = { isCustomMode = it },
        )

        val modeSuffix = if (isCustomMode) " (Custom UI)" else " (Material 3)"

        SourceButtons(
            modeSuffix = modeSuffix,
            onUrlClick = { onSourceSelected(PdfSource.Url(SAMPLE_URL), isCustomMode) },
            onAssetsClick = {
                onSourceSelected(
                    PdfSource.Local("file:///android_asset/sample.pdf"),
                    isCustomMode,
                )
            },
            onDeviceClick = { launcher.launch(arrayOf("application/pdf")) },
        )
    }
}

@Composable
private fun ModeToggle(
    isCustomMode: Boolean,
    onModeChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = { onModeChanged(false) },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (!isCustomMode) MaterialTheme.colorScheme.primary else Color.Gray,
                ),
        ) {
            Text("Material 3")
        }
        Button(
            onClick = { onModeChanged(true) },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (isCustomMode) MaterialTheme.colorScheme.primary else Color.Gray,
                ),
        ) {
            Text("Custom UI")
        }
    }
}

@Composable
private fun SourceButtons(
    modeSuffix: String,
    onUrlClick: () -> Unit,
    onAssetsClick: () -> Unit,
    onDeviceClick: () -> Unit,
) {
    Button(
        onClick = onUrlClick,
        modifier =
            Modifier
                .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                .padding(8.dp),
    ) {
        Text("Load from URL$modeSuffix")
    }

    Button(
        onClick = onAssetsClick,
        modifier =
            Modifier
                .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                .padding(8.dp),
    ) {
        Text("Load from Assets$modeSuffix")
    }

    Button(
        onClick = onDeviceClick,
        modifier =
            Modifier
                .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                .padding(8.dp),
    ) {
        Text("Load from Device$modeSuffix")
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
        val state = rememberPdfViewerState()
        val repository = rememberPdfRepository()

        LaunchedEffect(source, repository) {
            state.load(source, repository)
        }

        PdfViewer(
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            topBar = {
                Material3PdfToolbar(state = state, source = source)
            },
            loadingContent = {
                Material3LoadingContent()
            },
            passwordDialog = { isInvalid, onConfirm ->
                Material3PasswordDialog(isInvalid = isInvalid, onConfirm = onConfirm)
            },
        )
    }
}
