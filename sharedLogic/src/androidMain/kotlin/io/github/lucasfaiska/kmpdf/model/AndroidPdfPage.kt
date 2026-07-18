package io.github.lucasfaiska.kmpdf.model

import androidx.core.graphics.createBitmap
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEnginePage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

internal class AndroidPdfPage(
    private val enginePage: AndroidPdfEnginePage,
    private val dispatcher: CoroutineDispatcher,
) : PdfPage {
    override val width: Int = enginePage.width
    override val height: Int = enginePage.height

    override suspend fun render(
        width: Int,
        height: Int,
    ): ByteArray =
        withContext(dispatcher) {
            val bitmap = createBitmap(width, height)

            enginePage.render(bitmap)

            val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(byteBuffer)
            bitmap.recycle()

            byteBuffer.array()
        }

    fun close() {
        enginePage.close()
    }
}
