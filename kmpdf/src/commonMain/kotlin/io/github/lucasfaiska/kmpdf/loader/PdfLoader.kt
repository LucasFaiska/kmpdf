package io.github.lucasfaiska.kmpdf.loader

import io.github.lucasfaiska.kmpdf.model.PdfSource

interface PdfLoader {
    suspend fun load(source: PdfSource): ByteArray
}
