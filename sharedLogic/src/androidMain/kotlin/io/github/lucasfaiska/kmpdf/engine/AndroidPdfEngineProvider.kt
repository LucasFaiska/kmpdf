package io.github.lucasfaiska.kmpdf.engine

import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.ext.SdkExtensions

internal object AndroidPdfEngineProvider {
    fun provideEngine(
        pfd: ParcelFileDescriptor,
        password: String?,
    ): AndroidPdfEngine =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 13 -> {
                CompatPdfEngine(pfd, password)
            }

            else -> {
                ModernPdfEngine(pfd, password)
            }
        }
}
