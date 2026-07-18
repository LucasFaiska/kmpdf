package io.github.lucasfaiska.kmpdf.model

import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

internal class AndroidPdfDocument(
    private val engine: AndroidPdfEngine,
    private val tempFile: File,
    private val dispatcher: CoroutineDispatcher,
) : PdfDocument {
    private val mutex = Mutex()

    override val pageCount: Int = engine.pageCount

    override fun getPage(index: Int): PdfPage {
        return AndroidPdfPage(engine, index, dispatcher, mutex)
    }

    override fun close() {
        engine.close()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
