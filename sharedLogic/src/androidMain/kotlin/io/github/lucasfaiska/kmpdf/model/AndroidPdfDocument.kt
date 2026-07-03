package io.github.lucasfaiska.kmpdf.model

import android.graphics.pdf.PdfRenderer
import java.io.File

internal class AndroidPdfDocument(
    private val renderer: PdfRenderer,
    private val tempFile: File,
) : PdfDocument {
    override val pageCount: Int = renderer.pageCount

    override fun getPage(index: Int): PdfPage {
        val page = renderer.openPage(index)
        return AndroidPdfPage(page)
    }

    override fun close() {
        renderer.close()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
