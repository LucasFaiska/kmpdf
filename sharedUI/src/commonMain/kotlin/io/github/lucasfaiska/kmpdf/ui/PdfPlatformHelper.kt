package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import io.github.lucasfaiska.kmpdf.model.PdfSource

/**
 * Interface for platform-specific PDF actions like sharing and downloading.
 */
internal interface PdfPlatformHelper {
    /**
     * Shares the current PDF document using the platform's native share sheet.
     */
    fun share(source: PdfSource)

    /**
     * Downloads the PDF from a remote URL.
     */
    fun download(url: String)
}

@Composable
internal expect fun rememberPdfPlatformHelper(): PdfPlatformHelper
