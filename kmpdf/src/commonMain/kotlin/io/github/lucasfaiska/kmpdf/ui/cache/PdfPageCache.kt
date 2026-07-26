package io.github.lucasfaiska.kmpdf.ui.cache

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap

internal interface PdfPageCache {
    fun get(index: Int): ImageBitmap?

    fun put(
        index: Int,
        bitmap: ImageBitmap,
    )

    fun clear()
}

internal class PdfPageCacheImpl(
    private val maxSize: Int,
) : PdfPageCache {
    private val cache = mutableStateMapOf<Int, ImageBitmap>()
    private val keys = mutableListOf<Int>()

    override fun get(index: Int): ImageBitmap? {
        val bitmap = cache[index]
        if (bitmap != null) {
            keys.remove(index)
            keys.add(index)
        }
        return bitmap
    }

    override fun put(
        index: Int,
        bitmap: ImageBitmap,
    ) {
        if (cache.containsKey(index)) {
            keys.remove(index)
        } else if (cache.size >= maxSize) {
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
