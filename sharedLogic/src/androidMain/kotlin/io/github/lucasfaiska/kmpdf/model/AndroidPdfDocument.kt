package io.github.lucasfaiska.kmpdf.model

import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRendererPreV
import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

internal class AndroidPdfDocument(
    private val renderer: Any,
    private val tempFile: File,
    private val dispatcher: CoroutineDispatcher,
) : PdfDocument {
    override val pageCount: Int =
        when (renderer) {
            is PdfRenderer -> renderer.pageCount
            is PdfRendererPreV -> renderer.pageCount
            else -> 0
        }

    override fun getPage(index: Int): PdfPage =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && renderer is PdfRenderer) {
            AndroidPdfPage(renderer.openPage(index), dispatcher)
        } else if (renderer is PdfRendererPreV) {
            AndroidPdfPage(renderer.openPage(index), dispatcher)
        } else {
            throw IllegalStateException("Unsupported renderer type")
        }

    override fun close() {
        when (renderer) {
            is PdfRenderer -> renderer.close()
            is PdfRendererPreV -> renderer.close()
        }
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
