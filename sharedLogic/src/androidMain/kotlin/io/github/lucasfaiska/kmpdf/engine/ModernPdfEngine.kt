package io.github.lucasfaiska.kmpdf.engine

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer

internal class ModernPdfEngine(
    private val renderer: PdfRenderer,
) : AndroidPdfEngine {
    override val pageCount: Int get() = renderer.pageCount

    override fun openPage(index: Int): AndroidPdfEnginePage = ModernPdfEnginePage(renderer.openPage(index))

    override fun close() = renderer.close()
}

private class ModernPdfEnginePage(
    private val page: PdfRenderer.Page,
) : AndroidPdfEnginePage {
    override val width: Int get() = page.width
    override val height: Int get() = page.height

    override fun render(bitmap: Bitmap) {
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    }

    override fun close() = page.close()
}
