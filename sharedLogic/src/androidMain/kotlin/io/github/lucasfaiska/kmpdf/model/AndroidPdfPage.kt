package io.github.lucasfaiska.kmpdf.model

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

internal class AndroidPdfPage(
    private val rendererPage: PdfRenderer.Page
) : PdfPage {

    override val width: Int = rendererPage.width
    override val height: Int = rendererPage.height

    override suspend fun render(width: Int, height: Int): ByteArray = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

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
