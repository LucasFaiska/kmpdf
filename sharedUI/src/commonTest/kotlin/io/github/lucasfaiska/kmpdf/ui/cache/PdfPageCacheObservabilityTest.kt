package io.github.lucasfaiska.kmpdf.ui.cache

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.test.Test
import kotlin.test.assertTrue

class PdfPageCacheObservabilityTest {
    private class MockBitmap : ImageBitmap {
        override val colorSpace: androidx.compose.ui.graphics.colorspace.ColorSpace get() = throw NotImplementedError()
        override val config: androidx.compose.ui.graphics.ImageBitmapConfig get() = throw NotImplementedError()
        override val hasAlpha: Boolean get() = throw NotImplementedError()
        override val height: Int = 10
        override val width: Int = 10
        override fun prepareToDraw() {}
        override fun readPixels(buffer: IntArray, startX: Int, startY: Int, width: Int, height: Int, bufferOffset: Int, stride: Int) {}
    }

    @Test
    fun `given a cache when putting an item then it should trigger snapshot observers`() {
        val cache = PdfPageCacheImpl(5)
        var changeNotified = false

        val handle = Snapshot.registerApplyObserver { changed, _ ->
            // If the cache uses mutableStateMapOf, its internal state will be in the 'changed' set
            // or at least the apply observer will be triggered.
            changeNotified = true
        }

        try {
            Snapshot.withMutableSnapshot {
                cache.put(1, MockBitmap())
            }
            // Snapshot.sendApplyNotifications() might be needed in some environments
            Snapshot.sendApplyNotifications()
            
            assertTrue(changeNotified, "Cache update should trigger Snapshot apply notification. " +
                "If this fails, it means the cache is likely using a non-observable data structure (like mutableMapOf).")
        } finally {
            handle.dispose()
        }
    }
}
