package io.github.lucasfaiska.kmpdf.model

import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRendererPreV
import android.graphics.pdf.RenderParams
import android.os.Build
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

internal class AndroidPdfPage(
    private val rendererPage: Any,
    private val dispatcher: CoroutineDispatcher,
) : PdfPage {
    override val width: Int =
        when (rendererPage) {
            is PdfRenderer.Page -> rendererPage.width
            is PdfRendererPreV.Page -> rendererPage.width
            else -> 0
        }

    override val height: Int =
        when (rendererPage) {
            is PdfRenderer.Page -> rendererPage.height
            is PdfRendererPreV.Page -> rendererPage.height
            else -> 0
        }

    override suspend fun render(
        width: Int,
        height: Int,
    ): ByteArray =
        withContext(dispatcher) {
            val bitmap = createBitmap(width, height)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && rendererPage is PdfRenderer.Page) {
                rendererPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            } else if (rendererPage is PdfRendererPreV.Page) {
                val params = RenderParams.Builder(RenderParams.RENDER_MODE_FOR_DISPLAY).build()
                rendererPage.render(bitmap, null, null, params)
            } else if (rendererPage is PdfRenderer.Page) {
                rendererPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }

            val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(byteBuffer)
            bitmap.recycle()

            byteBuffer.array()
        }

    fun close() {
        when (rendererPage) {
            is PdfRenderer.Page -> rendererPage.close()
            is PdfRendererPreV.Page -> rendererPage.close()
        }
    }
}
