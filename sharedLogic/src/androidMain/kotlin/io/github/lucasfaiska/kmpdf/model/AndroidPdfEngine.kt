package io.github.lucasfaiska.kmpdf.model

import android.graphics.Bitmap
import android.graphics.pdf.LoadParams
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRendererPreV
import android.graphics.pdf.RenderParams
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension

internal interface AndroidPdfEngine {
    val pageCount: Int

    fun openPage(index: Int): AndroidPdfEnginePage

    fun close()

    companion object {
        operator fun invoke(
            pfd: ParcelFileDescriptor,
            password: String?,
        ): AndroidPdfEngine =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM -> {
                    val renderer =
                        if (password != null) {
                            val params = LoadParams.Builder().setPassword(password).build()
                            PdfRenderer(pfd, params)
                        } else {
                            PdfRenderer(pfd)
                        }
                    ModernPdfEngine(renderer)
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 13 -> {
                    val renderer =
                        if (password != null) {
                            val params = LoadParams.Builder().setPassword(password).build()
                            PdfRendererPreV(pfd, params)
                        } else {
                            PdfRendererPreV(pfd)
                        }
                    CompatPdfEngine(renderer)
                }

                else -> {
                    if (password != null) throw SecurityException("Password not supported")
                    ModernPdfEngine(PdfRenderer(pfd))
                }
            }
    }
}

internal interface AndroidPdfEnginePage {
    val width: Int
    val height: Int

    fun render(bitmap: Bitmap)

    fun close()
}

private class ModernPdfEngine(
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

@RequiresApi(Build.VERSION_CODES.R)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
private class CompatPdfEngine(
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
