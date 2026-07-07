package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.compositionLocalOf
import io.github.lucasfaiska.kmpdf.repository.PdfRepository

/**
 * CompositionLocal for [PdfRepository].
 */
val LocalPdfRepository =
    compositionLocalOf<PdfRepository> {
        error("No PdfRepository provided")
    }
