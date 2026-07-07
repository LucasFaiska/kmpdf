package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.reader.PdfReader

@Composable
actual fun rememberDefaultPdfLoader(): PdfLoader = TODO("iOS PdfLoader not implemented")

@Composable
actual fun rememberDefaultPdfReader(): PdfReader = TODO("iOS PdfReader not implemented")
