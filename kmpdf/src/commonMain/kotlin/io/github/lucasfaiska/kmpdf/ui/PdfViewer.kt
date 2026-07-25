package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfSource

/**
 * Displays a PDF document from a [PdfSource] with custom UI slots.
 *
 * @param state The state object that manages the PDF document and its display properties.
 * @param modifier The modifier to be applied to the layout.
 * @param topBar The content to be shown at the top of the viewer.
 * @param bottomBar The content to be shown at the bottom of the viewer.
 * @param loadingContent The content to be shown while the document is loading.
 * @param errorContent The content to be shown if an error occurs.
 * @param passwordDialog The content to be shown when a password is required.
 */
@Composable
fun PdfViewer(
    state: PdfViewerState,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (PdfError) -> Unit = {},
    passwordDialog: @Composable (isInvalid: Boolean, onConfirm: (String) -> Unit) -> Unit = { _, _ -> },
) {
    val repository = rememberPdfRepository()

    if (state.isPasswordRequired || state.isPasswordInvalid) {
        passwordDialog(state.isPasswordInvalid) { password ->
            state.unlock(password, repository)
        }
    }

    Column(modifier = modifier) {
        topBar()
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
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
        bottomBar()
    }
}

/**
 * High-level Composable to display a PDF document from a URL.
 */
@Composable
fun PdfViewer(
    url: String,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (PdfError) -> Unit = {},
    passwordDialog: @Composable (isInvalid: Boolean, onConfirm: (String) -> Unit) -> Unit = { _, _ -> },
) {
    PdfViewer(
        source = PdfSource.Url(url),
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        loadingContent = loadingContent,
        errorContent = errorContent,
        passwordDialog = passwordDialog,
    )
}

/**
 * High-level Composable to display a PDF document from a local identifier.
 */
@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    identifier: String,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (PdfError) -> Unit = {},
    passwordDialog: @Composable (isInvalid: Boolean, onConfirm: (String) -> Unit) -> Unit = { _, _ -> },
) {
    PdfViewer(
        source = PdfSource.Local(identifier),
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        loadingContent = loadingContent,
        errorContent = errorContent,
        passwordDialog = passwordDialog,
    )
}

/**
 * High-level Composable to display a PDF document from a [PdfSource].
 */
@Composable
fun PdfViewer(
    source: PdfSource,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (PdfError) -> Unit = {},
    passwordDialog: @Composable (isInvalid: Boolean, onConfirm: (String) -> Unit) -> Unit = { _, _ -> },
) {
    val state = rememberPdfViewerState()
    val repository = rememberPdfRepository()

    LaunchedEffect(source, repository) {
        state.load(source, repository)
    }

    PdfViewer(
        state = state,
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        loadingContent = loadingContent,
        errorContent = errorContent,
        passwordDialog = passwordDialog,
    )
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
        }
    }
}
