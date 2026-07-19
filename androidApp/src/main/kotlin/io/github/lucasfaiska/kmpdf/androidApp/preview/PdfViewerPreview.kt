package io.github.lucasfaiska.kmpdf.androidApp.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
