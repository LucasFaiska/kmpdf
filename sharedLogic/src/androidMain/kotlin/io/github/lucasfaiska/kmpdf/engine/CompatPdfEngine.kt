package io.github.lucasfaiska.kmpdf.engine

import android.graphics.Bitmap
import android.graphics.pdf.PdfRendererPreV
import android.graphics.pdf.RenderParams
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension

@RequiresApi(Build.VERSION_CODES.R)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
internal class CompatPdfEngine(
    private val renderer: PdfRendererPreV,
) : AndroidPdfEngine {
    override val pageCount: Int get() = renderer.pageCount

    override fun openPage(index: Int): AndroidPdfEnginePage = CompatPdfEnginePage(renderer.openPage(index))

    override fun close() = renderer.close()
}

@RequiresApi(Build.VERSION_CODES.R)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
private class CompatPdfEnginePage(
    private val page: PdfRendererPreV.Page,
) : AndroidPdfEnginePage {
    override val width: Int get() = page.width
    override val height: Int get() = page.height

    override fun render(bitmap: Bitmap) {
        val params = RenderParams.Builder(RenderParams.RENDER_MODE_FOR_DISPLAY).build()
        page.render(bitmap, null, null, params)
    }

    override fun close() = page.close()
}
