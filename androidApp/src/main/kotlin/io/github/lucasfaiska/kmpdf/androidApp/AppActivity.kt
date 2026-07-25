package io.github.lucasfaiska.kmpdf.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lucasfaiska.kmpdf.material3.Material3LoadingContent
import io.github.lucasfaiska.kmpdf.material3.Material3PasswordDialog
import io.github.lucasfaiska.kmpdf.material3.Material3PdfToolbar
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfViewer
import io.github.lucasfaiska.kmpdf.ui.rememberPdfRepository
import io.github.lucasfaiska.kmpdf.ui.rememberPdfViewerState

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
    var useCustomUi by remember { mutableStateOf(false) }

    if (selectedSource == null) {
        SelectionScreen(onSourceSelected = { source, isCustom ->
            selectedSource = source
            useCustomUi = isCustom
        })
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
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let { onSourceSelected(PdfSource.Local(it.toString()), false) }
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
                    false,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                    .padding(8.dp),
        ) {
            Text("Load from URL (Material 3)")
        }

        Button(
            onClick = {
                onSourceSelected(
                    PdfSource.Url(SAMPLE_URL),
                    true,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                    .padding(8.dp),
        ) {
            Text("Load from URL (Custom UI)")
        }

        Button(
            onClick = {
                onSourceSelected(
                    PdfSource.Local("file:///android_asset/sample.pdf"),
                    false,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                    .padding(8.dp),
        ) {
            Text("Load from Assets (Material 3)")
        }

        Button(
            onClick = { launcher.launch(arrayOf("application/pdf")) },
            modifier =
                Modifier
                    .fillMaxWidth(BUTTON_WIDTH_FRACTION)
                    .padding(8.dp),
        ) {
            Text("Load from Device (Material 3)")
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
        val state = rememberPdfViewerState()
        val repository = rememberPdfRepository()

        LaunchedEffect(source, repository) {
            state.load(source, repository)
        }

        PdfViewer(
            state = state,
            modifier = Modifier.fillMaxSize().padding(padding),
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

@Composable
fun CustomViewerScreen(
    source: PdfSource,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val state = rememberPdfViewerState()
    val repository = rememberPdfRepository()

    LaunchedEffect(source, repository) {
        state.load(source, repository)
    }

    PdfViewer(
        state = state,
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
        topBar = {
            // A totally custom toolbar without Material 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF333333))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "← Back",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = "Page ${state.currentPage} of ${state.pageCount}",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Row {
                    Text(
                        text = "-",
                        color = Color.White,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { state.zoomOut() },
                        fontSize = 20.sp
                    )
                    Text(
                        text = "+",
                        color = Color.White,
                        modifier = Modifier.clickable { state.zoomIn() },
                        fontSize = 20.sp
                    )
                }
            }
        },
        loadingContent = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Custom UI...", color = Color.Gray)
            }
        },
        passwordDialog = { isInvalid, onConfirm ->
            // A totally custom password dialog without Material 3 AlertDialog
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(Color.White)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "PASSWORD REQUIRED",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    if (isInvalid) {
                        Text("Incorrect!", color = Color.Red)
                    }
                    var password by remember { mutableStateOf("") }
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .background(Color.LightGray)
                            .padding(8.dp)
                    )
                    Text(
                        "UNLOCK",
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(8.dp)
                            .clickable { onConfirm(password) },
                        color = Color.White
                    )
                }
            }
        }
    )
}
