package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import io.github.lucasfaiska.kmpdf.ui.cache.LruPdfPageCache
import io.github.lucasfaiska.kmpdf.ui.cache.PdfPageCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State class for [PdfViewer] that manages document loading and page rendering.
 *
 * @param repository The [PdfRepository] used to load documents.
 * @param cache The [PdfPageCache] used for rendered pages.
 * @param coroutineScope The [CoroutineScope] for background tasks.
 */
@Stable
class PdfViewerState(
    private val repository: PdfRepository,
    private val cache: PdfPageCache,
    private val coroutineScope: CoroutineScope,
) {
    var document by mutableStateOf<PdfDocument?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<Throwable?>(null)
        private set

    /**
     * Loads a PDF document from the given [source].
     */
    fun load(source: PdfSource) {
        loading = true
        error = null
        coroutineScope.launch {
            try {
                val newDocument = repository.loadDocument(source)
                document?.close()
                document = newDocument
                cache.clear()
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
        val cached = cache.get(index)
        if (cached != null) return cached

        val doc = document ?: return null
        if (index >= doc.pageCount) return null

        LaunchedEffect(index, width, height) {
            try {
                val page = doc.getPage(index)
                val bytes = page.render(width, height)
                val bitmap = bytes.toImageBitmap(width, height)
                cache.put(index, bitmap)
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
        cache.clear()
    }
}

/**
 * Remembers a [PdfViewerState].
 *
 * @param repository The [PdfRepository] to use. Defaults to [LocalPdfRepository].
 * @param cacheSize The maximum number of pages to keep in memory.
 */
@Composable
fun rememberPdfViewerState(
    repository: PdfRepository = LocalPdfRepository.current,
    cacheSize: Int = 15,
): PdfViewerState {
    val scope = rememberCoroutineScope()
    val cache = remember(cacheSize) { LruPdfPageCache(cacheSize) }
    val state =
        remember(repository, cache, scope) {
            PdfViewerState(repository, cache, scope)
        }

    DisposableEffect(state) {
        onDispose {
            state.close()
        }
    }

    return state
}
