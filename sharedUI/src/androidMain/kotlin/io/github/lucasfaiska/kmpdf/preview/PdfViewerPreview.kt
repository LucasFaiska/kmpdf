package io.github.lucasfaiska.kmpdf.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import io.github.lucasfaiska.kmpdf.ui.PdfViewer

@Preview(showBackground = true)
@Composable
fun PdfViewerPreview() {
    PdfViewer(
        url = "https://example.com/sample.pdf",
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
fun PdfViewerLoadingPreview() {
    PdfViewer(
        url = "https://example.com/sample.pdf",
        modifier = Modifier.fillMaxSize(),
        showToolbar = true,
    )
}

private class MockPdfRepository : PdfRepository {
    override suspend fun loadDocument(
        source: PdfSource,
        password: String?,
    ): PdfDocument = throw NotImplementedError()
}
