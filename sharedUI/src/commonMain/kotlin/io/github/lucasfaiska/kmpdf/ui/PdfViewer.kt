package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfSource

/**
 * Displays a PDF document from a remote URL.
 *
 * @param url The URL of the PDF document.
 * @param modifier The modifier to be applied to the layout.
 * @param loadingContent The content to be shown while the document is loading.
 * @param errorContent The content to be shown if an error occurs.
 * @param showToolbar Whether to show the default toolbar with navigation and zoom controls.
 */
@Composable
fun PdfViewer(
    url: String,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (Throwable) -> Unit = {},
    showToolbar: Boolean = false,
) {
    val state = rememberPdfViewerState()
    val source = remember(url) { PdfSource.Url(url) }
    PdfViewerCore(
        source = source,
        modifier = modifier,
        state = state,
        showToolbar = showToolbar,
        loadingContent = loadingContent,
        errorContent = errorContent,
    )
}

/**
 * Displays a PDF document from a local identifier (e.g., assets or file path).
 *
 * @param modifier The modifier to be applied to the layout.
 * @param identifier The local identifier of the PDF document.
 * @param loadingContent The content to be shown while the document is loading.
 * @param errorContent The content to be shown if an error occurs.
 * @param showToolbar Whether to show the default toolbar with navigation and zoom controls.
 */
@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    identifier: String,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (Throwable) -> Unit = {},
    showToolbar: Boolean = false,
) {
    val state = rememberPdfViewerState()
    val source = remember(identifier) { PdfSource.Local(identifier) }
    PdfViewerCore(
        source = source,
        modifier = modifier,
        state = state,
        showToolbar = showToolbar,
        loadingContent = loadingContent,
        errorContent = errorContent,
    )
}

/**
 * Displays a PDF document from a [PdfSource].
 *
 * @param source The source of the PDF document.
 * @param modifier The modifier to be applied to the layout.
 * @param loadingContent The content to be shown while the document is loading.
 * @param errorContent The content to be shown if an error occurs.
 * @param showToolbar Whether to show the default toolbar with navigation and zoom controls.
 */
@Composable
fun PdfViewer(
    source: PdfSource,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (Throwable) -> Unit = {},
    showToolbar: Boolean = false,
) {
    val state = rememberPdfViewerState()
    val stableSource = remember(source) { source }
    PdfViewerCore(
        source = stableSource,
        modifier = modifier,
        state = state,
        showToolbar = showToolbar,
        loadingContent = loadingContent,
        errorContent = errorContent,
    )
}

@Composable
private fun PdfViewerCore(
    source: PdfSource,
    modifier: Modifier,
    state: PdfViewerState,
    showToolbar: Boolean,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (Throwable) -> Unit,
) {
    val repository = rememberPdfRepository()

    LaunchedEffect(source, repository) {
        state.load(source, repository)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showToolbar && !state.loading && state.error == null) {
                PdfToolbar(state = state, source = source)
            }
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
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
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier =
            modifier
                .onSizeChanged { containerSize = it }
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { _, pan, zoom, _ ->
                            if (zoom != 1f) {
                                state.updateZoom(zoom)
                            }
                            if (state.zoomScale > 1f) {
                                state.updateOffset(pan, containerSize)
                            }
                        },
                    )
                }.pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (state.zoomScale > 1f) {
                                state.resetZoom()
                            } else {
                                state.updateZoom(2f)
                            }
                        },
                    )
                },
    ) {
        LazyColumn(
            state = state.lazyListState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = state.zoomScale,
                        scaleY = state.zoomScale,
                        translationX = state.offset.x,
                        translationY = state.offset.y,
                    ),
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
