package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lucasfaiska.kmpdf.loader.AndroidPdfLoader
import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.reader.AndroidPdfReader
import io.github.lucasfaiska.kmpdf.reader.PdfReader
import kotlinx.coroutines.Dispatchers

@Composable
actual fun rememberDefaultPdfLoader(): PdfLoader {
    val context = LocalContext.current
    return remember(context) {
        AndroidPdfLoader(context)
    }
}

@Composable
actual fun rememberDefaultPdfReader(): PdfReader {
    val context = LocalContext.current
    return remember(context) {
        AndroidPdfReader(context, Dispatchers.IO)
    }
}
