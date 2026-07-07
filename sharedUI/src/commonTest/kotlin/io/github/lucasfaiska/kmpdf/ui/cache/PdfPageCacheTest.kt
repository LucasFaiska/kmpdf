package io.github.lucasfaiska.kmpdf.ui.cache

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PdfPageCacheTest {
    private class MockBitmap : ImageBitmap {
        override val colorSpace: androidx.compose.ui.graphics.colorspace.ColorSpace
            get() = throw NotImplementedError()
        override val config: androidx.compose.ui.graphics.ImageBitmapConfig
            get() = throw NotImplementedError()
        override val hasAlpha: Boolean
            get() = throw NotImplementedError()
        override val height: Int = 100
        override val width: Int = 100

        override fun prepareToDraw() {}

        override fun readPixels(
            buffer: IntArray,
            startX: Int,
            startY: Int,
            width: Int,
            height: Int,
            bufferOffset: Int,
            stride: Int,
        ) {}
    }

    @Test
    fun `given a cache when putting items then it should retrieve them`() {
        val cache = PdfPageCacheImpl(2)
        val bitmap1 = MockBitmap()

        cache.put(1, bitmap1)

        assertEquals(bitmap1, cache.get(1))
    }

    @Test
    fun `given a cache when exceeding max size then it should evict oldest`() {
        val cache = PdfPageCacheImpl(2)
        val bitmap1 = MockBitmap()
        val bitmap2 = MockBitmap()
        val bitmap3 = MockBitmap()

        cache.put(1, bitmap1)
        cache.put(2, bitmap2)
        cache.put(3, bitmap3)

        assertNull(cache.get(1))
        assertNotNull(cache.get(2))
        assertNotNull(cache.get(3))
    }

    @Test
    fun `given a cache when item is accessed then it should be moved to recently used`() {
        val cache = PdfPageCacheImpl(2)
        val bitmap1 = MockBitmap()
        val bitmap2 = MockBitmap()
        val bitmap3 = MockBitmap()

        cache.put(1, bitmap1)
        cache.put(2, bitmap2)
        cache.get(1)
        cache.put(3, bitmap3)

        assertNotNull(cache.get(1))
        assertNull(cache.get(2))
        assertNotNull(cache.get(3))
    }
}
