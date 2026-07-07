package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfSource

/**
 * Displays a PDF document from a remote URL.
 */
@Composable
fun PdfViewer(
    url: String,
    modifier: Modifier = Modifier,
    state: PdfViewerState = rememberPdfViewerState(),
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (Throwable) -> Unit = {},
) {
    PdfViewerCore(
        source = PdfSource.Url(url),
        modifier = modifier,
        state = state,
        loadingContent = loadingContent,
        errorContent = errorContent,
    )
}

/**
 * Displays a PDF document from a local identifier (e.g., assets or file path).
 */
@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    identifier: String,
    state: PdfViewerState = rememberPdfViewerState(),
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (Throwable) -> Unit = {},
) {
    PdfViewerCore(
        source = PdfSource.Local(identifier),
        modifier = modifier,
        state = state,
        loadingContent = loadingContent,
        errorContent = errorContent,
    )
}

/**
 * Displays a PDF document from a [PdfSource].
 */
@Composable
fun PdfViewer(
    source: PdfSource,
    modifier: Modifier = Modifier,
    state: PdfViewerState = rememberPdfViewerState(),
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (Throwable) -> Unit = {},
) {
    PdfViewerCore(
        source = source,
        modifier = modifier,
        state = state,
        loadingContent = loadingContent,
        errorContent = errorContent,
    )
}

@Composable
private fun PdfViewerCore(
    source: PdfSource,
    modifier: Modifier,
    state: PdfViewerState,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (Throwable) -> Unit,
) {
    val repository = rememberPdfRepository()

    LaunchedEffect(source, repository) {
        state.load(source, repository)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.loading -> {
                loadingContent()
            }

            state.error != null -> {
                errorContent(state.error!!)
            }

            state.document != null -> {
                PdfContent(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun DefaultLoadingContent() {
    CircularProgressIndicator()
}

@Composable
private fun PdfContent(
    state: PdfViewerState,
    modifier: Modifier,
) {
    val document = state.document ?: return
    val transformState = rememberTransformableState { _, _, _, _ -> }

    LazyColumn(
        modifier = modifier.transformable(state = transformState),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(document.pageCount) { index ->
            PdfPageItem(
                state = state,
                index = index,
            )
        }
    }
}

@Composable
private fun PdfPageItem(
    state: PdfViewerState,
    index: Int,
) {
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.707f),
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val bitmap: ImageBitmap? = state.getPage(index, width, height)

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}
