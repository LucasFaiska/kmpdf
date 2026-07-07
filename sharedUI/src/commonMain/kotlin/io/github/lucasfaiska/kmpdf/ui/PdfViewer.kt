package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.repository.PdfRepository

/**
 * A Composable that displays a PDF document from a given source.
 *
 * @param source The [PdfSource] to load the PDF from.
 * @param modifier The modifier to be applied to the layout.
 * @param repository The [PdfRepository] used to load the PDF. Defaults to [LocalPdfRepository].
 * @param state The state object to be used to control or observe the [PdfViewer] state.
 */
@Composable
fun PdfViewer(
    source: PdfSource,
    modifier: Modifier = Modifier,
    repository: PdfRepository = LocalPdfRepository.current,
    state: PdfViewerState =
        rememberPdfViewerState(
            repository = repository,
        ),
) {
    LaunchedEffect(source) {
        state.load(source)
    }

    var scale by remember { mutableStateOf(1f) }
    val transformState =
        rememberTransformableState { _, zoomChange, _, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 5f)
        }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.loading -> {
                CircularProgressIndicator()
            }
            state.error != null -> {
                Text("Error loading PDF: ${state.error?.message}")
            }
            state.document != null -> {
                PdfContent(
                    state = state,
                    scale = scale,
                    transformState = transformState,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * Internal content of the [PdfViewer] that renders the list of pages.
 */
@Composable
private fun PdfContent(
    state: PdfViewerState,
    scale: Float,
    transformState: TransformableState,
    modifier: Modifier = Modifier,
) {
    val document = state.document ?: return

    LazyColumn(
        modifier =
            modifier
                .transformable(state = transformState),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(document.pageCount) { index ->
            PdfPageItem(
                state = state,
                index = index,
                scale = scale,
            )
        }
    }
}

/**
 * A single page item in the PDF list.
 */
@Composable
private fun PdfPageItem(
    state: PdfViewerState,
    index: Int,
    scale: Float,
) {
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.707f)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                ),
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        val bitmap: ImageBitmap? = state.getPage(index, width, height)

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Page ${index + 1}",
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
