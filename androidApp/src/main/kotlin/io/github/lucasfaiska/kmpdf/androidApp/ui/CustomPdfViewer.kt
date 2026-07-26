package io.github.lucasfaiska.kmpdf.androidApp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
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
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfPlatformActions
import io.github.lucasfaiska.kmpdf.ui.PdfViewer
import io.github.lucasfaiska.kmpdf.ui.PdfViewerState
import io.github.lucasfaiska.kmpdf.ui.rememberPdfPlatformActions
import io.github.lucasfaiska.kmpdf.ui.rememberPdfRepository
import io.github.lucasfaiska.kmpdf.ui.rememberPdfViewerState

private const val PASSWORD_DIALOG_WIDTH_FRACTION = 0.8f
private val CUSTOM_UI_BACKGROUND = Color(0xFFF5F5F5)
private val CUSTOM_UI_TOOLBAR_COLOR = Color(0xFF333333)

@Composable
fun CustomViewerScreen(
    source: PdfSource,
    onBack: () -> Unit,
) {
    val state = rememberPdfViewerState()
    val repository = rememberPdfRepository()

    LaunchedEffect(source, repository) {
        state.load(source, repository)
    }

    PdfViewer(
        state = state,
        modifier = Modifier.fillMaxSize().background(CUSTOM_UI_BACKGROUND),
        topBar = {
            CustomToolbar(
                state = state,
                source = source,
                onBack = onBack,
            )
        },
        loadingContent = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Custom UI...", color = Color.Gray)
            }
        },
        passwordDialog = { isInvalid, onConfirm ->
            CustomPasswordDialog(isInvalid, onConfirm)
        },
    )
}

@Composable
private fun CustomToolbar(
    state: PdfViewerState,
    source: PdfSource,
    onBack: () -> Unit,
) {
    val platformActions = rememberPdfPlatformActions()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(CUSTOM_UI_TOOLBAR_COLOR)
                .padding(top = statusBarPadding),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "← Back",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBack() },
            )

            CustomNavigationControls(state)

            CustomZoomControls(state)

            CustomPlatformActions(source, platformActions)
        }
    }
}

@Composable
private fun CustomNavigationControls(state: PdfViewerState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "↑",
            color = Color.White,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { state.scrollToPage(state.currentPage - 2) },
            fontSize = 20.sp,
        )
        Text(
            text = "${state.currentPage} / ${state.pageCount}",
            color = Color.White,
            fontSize = 14.sp,
        )
        Text(
            text = "↓",
            color = Color.White,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { state.scrollToPage(state.currentPage) },
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun CustomZoomControls(state: PdfViewerState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "-",
            color = Color.White,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { state.zoomOut() },
            fontSize = 20.sp,
        )
        Text(
            text = "${(state.zoomScale * 100).toInt()}%",
            color = Color.White,
            fontSize = 14.sp,
        )
        Text(
            text = "+",
            color = Color.White,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { state.zoomIn() },
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun CustomPlatformActions(
    source: PdfSource,
    platformActions: PdfPlatformActions,
) {
    Row {
        if (source is PdfSource.Url) {
            Text(
                text = "↓",
                color = Color.White,
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { platformActions.download(source.url) },
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = "⇪",
            color = Color.White,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { platformActions.share(source) },
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CustomPasswordDialog(
    isInvalid: Boolean,
    onConfirm: (String) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(PASSWORD_DIALOG_WIDTH_FRACTION)
                    .background(Color.White)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "PASSWORD REQUIRED",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
            )
            if (isInvalid) {
                Text("Incorrect!", color = Color.Red)
            }
            var password by remember { mutableStateOf("") }
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .background(Color.LightGray)
                        .padding(8.dp),
            )
            Text(
                "UNLOCK",
                modifier =
                    Modifier
                        .background(Color.Black)
                        .padding(8.dp)
                        .clickable { onConfirm(password) },
                color = Color.White,
            )
        }
    }
}
