package io.github.lucasfaiska.kmpdf.ui.cache

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Interface for caching rendered PDF pages.
 */
interface PdfPageCache {
    /**
     * Gets a cached page bitmap.
     *
     * @param index The page index.
     * @return The cached [ImageBitmap], or null if not found.
     */
    fun get(index: Int): ImageBitmap?

    /**
     * Puts a page bitmap into the cache.
     *
     * @param index The page index.
     * @param bitmap The [ImageBitmap] to cache.
     */
    fun put(index: Int, bitmap: ImageBitmap)

    /**
     * Clears the cache.
     */
    fun clear()
}

/**
 * A simple LRU cache implementation for [ImageBitmap] in commonMain.
 *
 * @param maxSize The maximum number of items to keep in the cache.
 */
class LruPdfPageCache(private val maxSize: Int) : PdfPageCache {
    private val cache = mutableMapOf<Int, ImageBitmap>()
    private val keys = mutableListOf<Int>()

    override fun get(index: Int): ImageBitmap? {
        val bitmap = cache[index]
        if (bitmap != null) {
            // Move to end (most recently used)
            keys.remove(index)
            keys.add(index)
        }
        return bitmap
    }

    override fun put(index: Int, bitmap: ImageBitmap) {
        if (cache.containsKey(index)) {
            keys.remove(index)
        } else if (cache.size >= maxSize) {
            // Remove least recently used
            val oldestKey = keys.removeAt(0)
            cache.remove(oldestKey)
        }
        cache[index] = bitmap
        keys.add(index)
    }

    override fun clear() {
        cache.clear()
        keys.clear()
    }
}
