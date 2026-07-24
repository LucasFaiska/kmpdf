package io.github.lucasfaiska.kmpdf.model

import android.graphics.Bitmap
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngine
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEnginePage
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class AndroidPdfDocumentTest {

    private class MockPdfEngine : AndroidPdfEngine {
        override val pageCount: Int = 5
        var closed = false
        
        override fun openPage(index: Int): AndroidPdfEnginePage = object : AndroidPdfEnginePage {
            override val width: Int = 100
            override val height: Int = 200
            override fun render(bitmap: Bitmap) {}
            override fun close() {}
        }
        
        override fun width(index: Int): Int = 100
        override fun height(index: Int): Int = 200
        override fun close() { closed = true }
    }

    @Test
    fun `test AndroidPdfDocument basic properties`() {
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

    private fun assertTrue(condition: Boolean) = org.junit.Assert.assertTrue(condition)
    private fun assertFalse(condition: Boolean) = org.junit.Assert.assertFalse(condition)
}
