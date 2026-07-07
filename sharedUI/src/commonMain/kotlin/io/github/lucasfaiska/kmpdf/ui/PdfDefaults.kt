package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import io.github.lucasfaiska.kmpdf.repository.PdfRepository

/**
 * Provides a default [PdfRepository] for the current platform.
 */
@Composable
expect fun rememberDefaultPdfRepository(): PdfRepository
