package io.github.lucasfaiska.kmpdf.repository

import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.reader.PdfReader

interface PdfRepository {
    /**
     * Loads a PDF document from the provided source.
     *
     * @param source The [PdfSource] to load from.
     * @param password An optional password for encrypted documents.
     * @return A [PdfDocument] instance.
     */
    suspend fun loadDocument(source: PdfSource, password: String? = null): PdfDocument
}

class PdfRepositoryImpl(
    private val loader: PdfLoader,
    private val reader: PdfReader,
) : PdfRepository {
    override suspend fun loadDocument(source: PdfSource, password: String?): PdfDocument {
        val bytes = loader.load(source)
        return reader.open(bytes, password)
    }
}
