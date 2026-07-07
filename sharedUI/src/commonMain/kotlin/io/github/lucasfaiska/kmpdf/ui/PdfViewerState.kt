package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.reader.PdfReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State class for [PdfViewer] that manages document loading and page rendering.
 */
@Stable
class PdfViewerState(
    private val loader: PdfLoader,
    private val reader: PdfReader,
    private val coroutineScope: CoroutineScope,
) {
    var document by mutableStateOf<PdfDocument?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<Throwable?>(null)
        private set

    private val pageCache = mutableStateMapOf<Int, ImageBitmap>()

    /**
     * Loads a PDF document from the given [source].
     */
    fun load(source: PdfSource) {
        loading = true
        error = null
        coroutineScope.launch {
            try {
                val bytes = loader.load(source)
                val newDocument = reader.open(bytes)
                document?.close()
                document = newDocument
                pageCache.clear()
            } catch (e: Exception) {
                error = e
            } finally {
                loading = false
            }
        }
    }

    /**
     * Gets or renders a page bitmap.
     *
     * @param index The 0-based page index.
     * @param width The target width for rendering.
     * @param height The target height for rendering.
     */
    @Composable
    fun getPage(
        index: Int,
        width: Int,
        height: Int,
    ): ImageBitmap? {
        val cached = pageCache[index]
        if (cached != null) return cached

        val doc = document ?: return null
        if (index >= doc.pageCount) return null

        LaunchedEffect(index, width, height) {
            try {
                val page = doc.getPage(index)
                val bytes = page.render(width, height)
                val bitmap = bytes.toImageBitmap(width, height)
                pageCache[index] = bitmap
            } catch (e: Exception) {
                // Error handling to be refined
            }
        }

        return null
    }

    /**
     * Releases document resources.
     */
    fun close() {
        document?.close()
        document = null
        pageCache.clear()
    }
}

@Composable
fun rememberPdfViewerState(
    loader: PdfLoader = rememberDefaultPdfLoader(),
    reader: PdfReader = rememberDefaultPdfReader(),
): PdfViewerState {
    val scope = rememberCoroutineScope()
    val state =
        remember(loader, reader, scope) {
            PdfViewerState(loader, reader, scope)
        }

    DisposableEffect(state) {
        onDispose {
            state.close()
        }
    }

    return state
}
