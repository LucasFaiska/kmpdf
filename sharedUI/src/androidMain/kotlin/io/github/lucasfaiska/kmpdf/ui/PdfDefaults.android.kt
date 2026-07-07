package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lucasfaiska.kmpdf.loader.AndroidPdfLoader
import io.github.lucasfaiska.kmpdf.reader.AndroidPdfReader
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import io.github.lucasfaiska.kmpdf.repository.PdfRepositoryImpl
import kotlinx.coroutines.Dispatchers

@Composable
internal actual fun rememberPdfRepository(): PdfRepository {
    val context = LocalContext.current
    return remember(context) {
        val loader = AndroidPdfLoader(context)
        val reader = AndroidPdfReader(context, Dispatchers.IO)
        PdfRepositoryImpl(loader, reader)
    }
}
