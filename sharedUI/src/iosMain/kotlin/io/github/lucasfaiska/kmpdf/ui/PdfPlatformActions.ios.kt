package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.lucasfaiska.kmpdf.model.PdfSource

internal class IosPdfPlatformActions : PdfPlatformActions {
    override fun share(source: PdfSource) {
        // Implementation postponed until PDF rendering is ready on iOS
    }

    override fun download(url: String) {
        // Implementation postponed until PDF rendering is ready on iOS
    }
}

@Composable
internal actual fun rememberPdfPlatformActions(): PdfPlatformActions = remember { IosPdfPlatformActions() }
