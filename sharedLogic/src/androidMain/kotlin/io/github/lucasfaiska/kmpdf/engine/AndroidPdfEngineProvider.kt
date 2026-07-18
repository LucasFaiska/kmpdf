package io.github.lucasfaiska.kmpdf.engine

import android.graphics.pdf.LoadParams
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRendererPreV
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.ext.SdkExtensions

internal object AndroidPdfEngineProvider {
    fun provideEngine(
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
