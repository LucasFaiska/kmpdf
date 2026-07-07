package io.github.lucasfaiska.kmpdf.repository

import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.reader.PdfReader

/**
 * Default implementation of [PdfRepository].
 */
class RealPdfRepository(
    private val loader: PdfLoader,
    private val reader: PdfReader,
) : PdfRepository {
    override suspend fun loadDocument(source: PdfSource): PdfDocument {
        val bytes = loader.load(source)
        return reader.open(bytes)
    }
}
