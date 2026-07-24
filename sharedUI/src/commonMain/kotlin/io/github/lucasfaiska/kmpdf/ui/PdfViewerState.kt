package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import io.github.lucasfaiska.kmpdf.ui.cache.PdfPageCache
import io.github.lucasfaiska.kmpdf.ui.cache.PdfPageCacheImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * State object for the [PdfViewer] component.
 * Manages document loading, page scrolling, and zoom level.
 *
 * @property lazyListState The state of the scrollable list of pages.
 */
@Stable
internal class PdfViewerState internal constructor(
    private val cache: PdfPageCache,
    private val coroutineScope: CoroutineScope,
    val lazyListState: LazyListState = LazyListState(),
) {
    /**
     * The currently loaded PDF document.
     */
    var document by mutableStateOf<PdfDocument?>(null)
        private set

    /**
     * Whether the document is currently loading.
     */
    var loading by mutableStateOf(false)
        private set

    /**
     * The error that occurred while loading the document, if any.
     */
    var error by mutableStateOf<PdfError?>(null)
        private set

    /**
     * Whether a password is required to open the document.
     */
    var isPasswordRequired by mutableStateOf(false)
        private set

    /**
     * Whether the provided password was incorrect.
     */
    var isPasswordInvalid by mutableStateOf(false)
        private set

    /**
     * The current zoom level of the viewer.
     */
    var zoomScale by mutableFloatStateOf(1f)
        private set

    /**
     * The current scroll offset of the zoomed page.
     */
    var offset by mutableStateOf(Offset.Zero)
        private set

    /**
     * The current visible page number (1-based).
     */
    val currentPage: Int
        get() = lazyListState.firstVisibleItemIndex + 1

    /**
     * The total number of pages in the document.
     */
    val pageCount: Int
        get() = document?.pageCount ?: 0

    private var currentSource: PdfSource? = null

    internal fun load(
        source: PdfSource,
        repository: PdfRepository,
        password: String? = null,
    ) {
        if (currentSource == source && password == null && (loading || document != null)) return

        currentSource = source
        loading = true
        error = null
        isPasswordRequired = false
        isPasswordInvalid = false

        coroutineScope.launch {
            try {
                when (val status = repository.loadDocument(source, password)) {
                    is PdfLoadStatus.Success -> {
                        document?.close()
                        document = status.document
                        cache.clear()
                        zoomScale = 1f
                        offset = Offset.Zero
                    }
                    PdfLoadStatus.PasswordRequired -> {
                        isPasswordRequired = true
                    }
                    PdfLoadStatus.InvalidPassword -> {
                        isPasswordInvalid = true
                    }
                    is PdfLoadStatus.Error -> {
                        error = status.error
                        currentSource = null
                    }
                }
            } catch (e: Exception) {
                error = PdfError(PdfErrorType.GENERIC, e.message, e)
                currentSource = null
            } finally {
                loading = false
            }
        }
    }

    /**
     * Attempts to unlock the document with the provided password.
     */
    fun unlock(
        password: String,
        repository: PdfRepository,
    ) {
        val source = currentSource ?: return
        load(source, repository, password)
    }

    /**
     * Scrolls to a specific page.
     *
     * @param index The 0-based index of the page.
     */
    fun scrollToPage(index: Int) {
        val target = max(0, min(index, pageCount - 1))
        coroutineScope.launch {
            lazyListState.animateScrollToItem(target)
        }
    }

    /**
     * Increases the zoom level.
     */
    fun zoomIn() {
        zoomScale = min(zoomScale + 0.25f, 5f)
    }

    /**
     * Decreases the zoom level.
     */
    fun zoomOut() {
        zoomScale = max(zoomScale - 0.25f, 1f)
    }

    /**
     * Resets the zoom level to 1x.
     */
    fun resetZoom() {
        zoomScale = 1f
        offset = Offset.Zero
    }

    /**
     * Updates the zoom level by a multiplier.
     *
     * @param scale The multiplier to apply to the current zoom level.
     */
    fun updateZoom(scale: Float) {
        zoomScale = max(1f, min(zoomScale * scale, 5f))
        if (zoomScale == 1f) {
            offset = Offset.Zero
        }
    }

    /**
     * Updates the scroll offset of the zoomed page.
     *
     * @param delta The amount to move the offset.
     * @param containerSize The size of the container.
     */
    fun updateOffset(
        delta: Offset,
        containerSize: IntSize,
    ) {
        if (zoomScale > 1f) {
            val maxOffsetX = (containerSize.width * (zoomScale - 1f)) / 2f
            val maxOffsetY = (containerSize.height * (zoomScale - 1f)) / 2f

            val newX = (offset.x + delta.x).coerceIn(-maxOffsetX, maxOffsetX)
            val newY = (offset.y + delta.y).coerceIn(-maxOffsetY, maxOffsetY)

            offset = Offset(newX, newY)
        } else {
            offset = Offset.Zero
        }
    }

    @Composable
    internal fun getPage(
        index: Int,
        width: Int,
        height: Int,
    ): ImageBitmap? {
        if (width <= 0 || height <= 0) return null

        val cached = cache.get(index)
        if (cached != null) return cached

        val doc = document ?: return null
        if (index >= doc.pageCount) return null

        LaunchedEffect(index, width, height) {
            try {
                doc.getPage(index).use { page ->
                    val bytes = page.render(width, height)
                    val bitmap = bytes.toImageBitmap(width, height)
                    cache.put(index, bitmap)
                }
            } catch (_: Exception) {
            }
        }

        return null
    }

    /**
     * Closes the document and clears the cache.
     */
    fun close() {
        document?.close()
        document = null
        cache.clear()
    }
}

/**
 * Creates and remembers a [PdfViewerState] instance.
 *
 * @param cacheSize The maximum number of pages to keep in memory.
 * @param lazyListState The state of the scrollable list.
 */
@Composable
internal fun rememberPdfViewerState(
    cacheSize: Int = 15,
    lazyListState: LazyListState = rememberLazyListState(),
): PdfViewerState {
    val scope = rememberCoroutineScope()
    val cache = remember(cacheSize) { PdfPageCacheImpl(cacheSize) }

    val state =
        remember(cache, scope, lazyListState) {
            PdfViewerState(cache, scope, lazyListState)
        }

    DisposableEffect(state) {
        onDispose {
            state.close()
        }
    }

    return state
}
