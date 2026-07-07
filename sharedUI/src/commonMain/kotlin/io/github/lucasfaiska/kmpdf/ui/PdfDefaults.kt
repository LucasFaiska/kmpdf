package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import io.github.lucasfaiska.kmpdf.repository.PdfRepository

@Composable
internal expect fun rememberPdfRepository(): PdfRepository
