package io.github.lucasfaiska.kmpdf.engine

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ModernPdfEngineTest {
    private class FakeNativePage(
        override val width: Int = 100,
        override val height: Int = 200,
    ) : NativePdfPageWrapper {
        var renderCalled = false
        var closed = false

        override fun render(bitmap: Bitmap) {
            renderCalled = true
        }

        override fun close() {
            closed = true
        }
    }

    private class FakeNativeEngine(
        override val pageCount: Int = 5,
        val page: FakeNativePage = FakeNativePage(),
    ) : NativePdfEngineWrapper {
        var closed = false

        override fun openPage(index: Int) = page

        override fun close() {
            closed = true
        }
    }

    @Test
    fun `given a fake native engine when modern engine is used then it should proxy properties and open pages correctly`() {
        val fakeNative = FakeNativeEngine()
        val engine = ModernPdfEngine(fakeNative)

        assertEquals(5, engine.pageCount)
        assertEquals(100, engine.width(0))
        assertEquals(200, engine.height(0))

        val page = engine.openPage(0)
        assertNotNull(page)
        assertEquals(100, page.width)
        assertEquals(200, page.height)

        engine.close()
        assertTrue(fakeNative.closed)
    }

    @Test
    fun `given a modern engine page when render is called then it should delegate to native wrapper`() {
        val fakePage = FakeNativePage()
        val page = ModernPdfEnginePage(fakePage)
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        page.render(bitmap)
        page.close()

        assertTrue(fakePage.renderCalled)
        assertTrue(fakePage.closed)
    }
}
