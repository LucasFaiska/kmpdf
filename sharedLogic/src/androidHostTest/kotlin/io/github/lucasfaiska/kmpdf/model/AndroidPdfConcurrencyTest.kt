package io.github.lucasfaiska.kmpdf.model

import android.graphics.Bitmap
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngine
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEnginePage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfConcurrencyTest {

    private class FakeEngine : AndroidPdfEngine {
        var openPageCount = 0
        val renderCallCount = AtomicInteger(0)
        var maxConcurrentOpenPages = 0

        override val pageCount: Int = 10

        override fun openPage(index: Int): AndroidPdfEnginePage {
            openPageCount++
            if (openPageCount > maxConcurrentOpenPages) {
                maxConcurrentOpenPages = openPageCount
            }
            return FakePage(this)
        }

        override fun width(index: Int): Int = 100
        override fun height(index: Int): Int = 100
        override fun close() {}

        fun pageClosed() {
            openPageCount--
        }
    }

    private class FakePage(val engine: FakeEngine) : AndroidPdfEnginePage {
        override val width: Int = 100
        override val height: Int = 100

        override fun render(bitmap: Bitmap) {
            engine.renderCallCount.incrementAndGet()
        }

        override fun close() {
            engine.pageClosed()
        }
    }

    @Test
    fun `given concurrent render calls when rendering pages then engine should only have one page open at a time`() = runTest {
        val engine = FakeEngine()
        val tempFile = File.createTempFile("test", ".pdf")
        val document = AndroidPdfDocument(engine, tempFile, Dispatchers.Unconfined)
        
        val pages = (0 until 5).map { document.getPage(it) }

        val results = pages.map { page ->
            async {
                page.render(100, 100)
            }
        }.awaitAll()

        assertEquals(5, results.size)
        assertEquals(5, engine.renderCallCount.get())
        assertEquals(1, engine.maxConcurrentOpenPages)
        
        document.close()
    }
}
