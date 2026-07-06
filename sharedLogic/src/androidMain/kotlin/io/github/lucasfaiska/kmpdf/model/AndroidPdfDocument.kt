package io.github.lucasfaiska.kmpdf.model

import android.graphics.pdf.PdfRenderer
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

internal class AndroidPdfDocument(
    private val renderer: PdfRenderer,
    private val tempFile: File,
    private val dispatcher: CoroutineDispatcher,
) : PdfDocument {
    override val pageCount: Int = renderer.pageCount

    override fun getPage(index: Int): PdfPage {
        val page = renderer.openPage(index)
        return AndroidPdfPage(page, dispatcher)
    }

    override fun close() {
        renderer.close()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
