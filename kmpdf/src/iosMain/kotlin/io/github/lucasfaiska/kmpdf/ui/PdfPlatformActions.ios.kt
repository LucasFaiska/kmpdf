package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.lucasfaiska.kmpdf.model.PdfSource

internal class IosPdfPlatformActions : PdfPlatformActions {
    override fun share(source: PdfSource) {
    }

    override fun download(url: String) {
    }
}

@Composable
actual fun rememberPdfPlatformActions(): PdfPlatformActions = remember { IosPdfPlatformActions() }
