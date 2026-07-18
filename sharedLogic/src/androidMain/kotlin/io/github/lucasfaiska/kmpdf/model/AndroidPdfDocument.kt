package io.github.lucasfaiska.kmpdf.model

import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngine
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

internal class AndroidPdfDocument(
    private val engine: AndroidPdfEngine,
    private val tempFile: File,
    private val dispatcher: CoroutineDispatcher,
) : PdfDocument {
    override val pageCount: Int = engine.pageCount

    override fun getPage(index: Int): PdfPage {
        val enginePage = engine.openPage(index)
        return AndroidPdfPage(enginePage, dispatcher)
    }

    override fun close() {
        engine.close()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
