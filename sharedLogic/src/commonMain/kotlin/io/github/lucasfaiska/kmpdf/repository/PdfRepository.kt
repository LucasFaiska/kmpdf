package io.github.lucasfaiska.kmpdf.repository

import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource

interface PdfRepository {
    suspend fun loadDocument(source: PdfSource): PdfDocument
}
