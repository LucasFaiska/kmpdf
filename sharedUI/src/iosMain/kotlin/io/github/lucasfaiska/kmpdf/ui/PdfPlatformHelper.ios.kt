package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.lucasfaiska.kmpdf.model.PdfSource
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal class IosPdfPlatformHelper : PdfPlatformHelper {
    override fun share(source: PdfSource) {
        val items =
            when (source) {
                is PdfSource.Url -> listOf(NSURL(string = source.url))
                is PdfSource.Local -> listOf(source.identifier)
            }
        val activityController = UIActivityViewController(items, null)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            activityController,
            animated = true,
            completion = null,
        )
    }

    override fun download(url: String) {
        val nsUrl = NSURL(string = url)
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}

@Composable
internal actual fun rememberPdfPlatformHelper(): PdfPlatformHelper = remember { IosPdfPlatformHelper() }
