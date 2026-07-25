package io.github.lucasfaiska.kmpdf.model

import android.graphics.Bitmap
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngine
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEnginePage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfDocumentTest {
    private class MockPdfEngine : AndroidPdfEngine {
        override val pageCount: Int = 5
        var closed = false
        var lastRenderBitmap: Bitmap? = null

        override fun openPage(index: Int): AndroidPdfEnginePage =
            object : AndroidPdfEnginePage {
                override val width: Int = 100
                override val height: Int = 200

                override fun render(bitmap: Bitmap) {
                    lastRenderBitmap = bitmap
                }

                override fun close() {}
            }

        override fun width(index: Int): Int = 100

        override fun height(index: Int): Int = 200

        override fun close() {
            closed = true
        }
    }

    @Test
    fun `given a valid engine and temp file when document is initialized then it should proxy properties correctly`() {
        val engine = MockPdfEngine()
        val tempFile = File.createTempFile("test", ".pdf")
        val document = AndroidPdfDocument(engine, tempFile, Dispatchers.Unconfined)

        assertEquals(5, document.pageCount)
        val page = document.getPage(0)
        assertNotNull(page)
        assertTrue(page is AndroidPdfPage)

        document.close()
        assertTrue(engine.closed)
        assertFalse(tempFile.exists())
    }

    @Test
    fun `given a valid page when render is called then it should create a bitmap and delegate to engine`() =
        runTest {
            val engine = MockPdfEngine()
            val tempFile = File.createTempFile("test_render", ".pdf")
            val document = AndroidPdfDocument(engine, tempFile, Dispatchers.Unconfined)
            val page = document.getPage(0)

            val result = page.render(100, 200)

            assertNotNull(result)
            assertNotNull(engine.lastRenderBitmap)
            assertEquals(100, engine.lastRenderBitmap?.width)
            assertEquals(200, engine.lastRenderBitmap?.height)

            document.close()
        }

    private fun assertTrue(condition: Boolean) = org.junit.Assert.assertTrue(condition)

    private fun assertFalse(condition: Boolean) = org.junit.Assert.assertFalse(condition)
}
