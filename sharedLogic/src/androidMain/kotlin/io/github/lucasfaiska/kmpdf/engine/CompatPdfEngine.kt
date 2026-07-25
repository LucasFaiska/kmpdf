package io.github.lucasfaiska.kmpdf.engine

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRendererPreV
import android.graphics.pdf.RenderParams

internal class CompatPdfEngine(
    private val native: NativePdfEngineWrapper,
) : AndroidPdfEngine {
    override val pageCount: Int get() = native.pageCount

    override fun openPage(index: Int): AndroidPdfEnginePage = CompatPdfEnginePage(native.openPage(index))

    override fun width(index: Int): Int = openPage(index).use { it.width }

    override fun height(index: Int): Int = openPage(index).use { it.height }

    override fun close() = native.close()
}

internal class CompatPdfEnginePage(
    private val native: NativePdfPageWrapper,
) : AndroidPdfEnginePage {
    override val width: Int get() = native.width

    override val height: Int get() = native.height

    override fun render(bitmap: Bitmap) = native.render(bitmap)

    override fun close() = native.close()
}

internal class RealCompatNativeEngine(
    private val renderer: PdfRendererPreV,
) : NativePdfEngineWrapper {
    @SuppressLint("NewApi")
    override val pageCount: Int = renderer.pageCount

    @SuppressLint("NewApi")
    override fun openPage(index: Int) = RealCompatNativePage(renderer.openPage(index))

    @SuppressLint("NewApi")
    override fun close() = renderer.close()
}

internal class RealCompatNativePage(
    private val page: PdfRendererPreV.Page,
) : NativePdfPageWrapper {
    @SuppressLint("NewApi")
    override val width: Int = page.width

    @SuppressLint("NewApi")
    override val height: Int = page.height

    @SuppressLint("NewApi")
    override fun render(bitmap: Bitmap) {
        val params = RenderParams.Builder(RenderParams.RENDER_MODE_FOR_DISPLAY).build()
        page.render(bitmap, null, null, params)
    }

    @SuppressLint("NewApi")
    override fun close() = page.close()
}
