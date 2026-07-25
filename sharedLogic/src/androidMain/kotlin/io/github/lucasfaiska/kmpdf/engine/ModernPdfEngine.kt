package io.github.lucasfaiska.kmpdf.engine

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer

internal class ModernPdfEngine(
    private val native: NativePdfEngineWrapper,
) : AndroidPdfEngine {
    override val pageCount: Int get() = native.pageCount

    override fun openPage(index: Int): AndroidPdfEnginePage = ModernPdfEnginePage(native.openPage(index))

    override fun width(index: Int): Int = openPage(index).use { it.width }

    override fun height(index: Int): Int = openPage(index).use { it.height }

    override fun close() = native.close()
}

internal class ModernPdfEnginePage(
    private val native: NativePdfPageWrapper,
) : AndroidPdfEnginePage {
    override val width: Int get() = native.width

    override val height: Int get() = native.height

    override fun render(bitmap: Bitmap) = native.render(bitmap)

    override fun close() = native.close()
}

internal class RealModernNativeEngine(
    private val renderer: PdfRenderer,
) : NativePdfEngineWrapper {
    override val pageCount: Int get() = renderer.pageCount

    override fun openPage(index: Int) = RealModernNativePage(renderer.openPage(index))

    override fun close() = renderer.close()
}

internal class RealModernNativePage(
    private val page: PdfRenderer.Page,
) : NativePdfPageWrapper {
    override val width: Int get() = page.width

    override val height: Int get() = page.height

    override fun render(bitmap: Bitmap) = page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

    override fun close() = page.close()
}
