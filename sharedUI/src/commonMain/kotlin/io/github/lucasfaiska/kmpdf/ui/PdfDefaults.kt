package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.reader.PdfReader

/**
 * Provides a default [PdfLoader] for the current platform.
 */
@Composable
expect fun rememberDefaultPdfLoader(): PdfLoader

/**
 * Provides a default [PdfReader] for the current platform.
 */
@Composable
expect fun rememberDefaultPdfReader(): PdfReader
