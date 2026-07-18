package io.github.lucasfaiska.kmpdf.engine

import android.graphics.Bitmap
import android.graphics.pdf.LoadParams
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor

internal class ModernPdfEngine(
    pfd: ParcelFileDescriptor,
    password: String?,
) : AndroidPdfEngine {
    private val renderer: PdfRenderer =
        if (password != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val params = LoadParams.Builder().setPassword(password).build()
            PdfRenderer(pfd, params)
        } else {
            PdfRenderer(pfd)
        }

    override val pageCount: Int get() = renderer.pageCount

    override fun openPage(index: Int): AndroidPdfEnginePage = ModernPdfEnginePage(renderer.openPage(index))

    override fun width(index: Int): Int = renderer.openPage(index).use { it.width }
    override fun height(index: Int): Int = renderer.openPage(index).use { it.height }

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
