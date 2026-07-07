package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lucasfaiska.kmpdf.loader.AndroidPdfLoader
import io.github.lucasfaiska.kmpdf.reader.AndroidPdfReader
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import io.github.lucasfaiska.kmpdf.repository.RealPdfRepository
import kotlinx.coroutines.Dispatchers

@Composable
actual fun rememberDefaultPdfRepository(): PdfRepository {
    val context = LocalContext.current
    return remember(context) {
        val loader = AndroidPdfLoader(context)
        val reader = AndroidPdfReader(context, Dispatchers.IO)
        RealPdfRepository(loader, reader)
    }
}
