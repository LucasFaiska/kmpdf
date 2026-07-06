package io.github.lucasfaiska.kmpdf.model

import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

internal class AndroidPdfPage(
    private val rendererPage: PdfRenderer.Page,
    private val dispatcher: CoroutineDispatcher,
) : PdfPage {
    override val width: Int = rendererPage.width
    override val height: Int = rendererPage.height

    override suspend fun render(
        width: Int,
        height: Int,
    ): ByteArray =
        withContext(dispatcher) {
            val bitmap = createBitmap(width, height)

            rendererPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(byteBuffer)
            bitmap.recycle()

            byteBuffer.array()
        }

    fun close() {
        rendererPage.close()
    }
}
