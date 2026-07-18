package io.github.lucasfaiska.kmpdf.model

import androidx.core.graphics.createBitmap
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

internal class AndroidPdfPage(
    private val engine: AndroidPdfEngine,
    private val index: Int,
    private val dispatcher: CoroutineDispatcher,
    private val mutex: Mutex,
) : PdfPage {

    override val width: Int get() = engine.width(index)
    override val height: Int get() = engine.height(index)

    override suspend fun render(
        width: Int,
        height: Int,
    ): ByteArray =
        mutex.withLock {
            withContext(dispatcher) {
                val page = engine.openPage(index)
                val bitmap = createBitmap(width, height)

                page.render(bitmap)

                val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
                bitmap.copyPixelsToBuffer(byteBuffer)
                bitmap.recycle()
                page.close()

                byteBuffer.array()
            }
        }

    override fun close() {
        // No-op as we open and close per render to ensure strict single-page usage
    }
}
