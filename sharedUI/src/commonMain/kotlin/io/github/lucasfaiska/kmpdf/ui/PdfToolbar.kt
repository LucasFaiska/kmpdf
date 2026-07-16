package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfSource

/**
 * Default toolbar for the [PdfViewer].
 * Provides controls for navigation, zoom, and platform-specific actions.
 */
@Composable
internal fun PdfToolbar(
    state: PdfViewerState,
    source: PdfSource,
    modifier: Modifier = Modifier,
) {
    val platformHelper = rememberPdfPlatformHelper()

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { state.scrollToPage(state.currentPage - 2) }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Página Anterior")
                }
                Text(
                    text = "${state.currentPage} / ${state.pageCount}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = { state.scrollToPage(state.currentPage) }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Próxima Página")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { state.zoomOut() }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Diminuir Zoom")
                }
                Text(
                    text = "${(state.zoomScale * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.widthIn(min = 40.dp),
                )
                IconButton(onClick = { state.zoomIn() }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Aumentar Zoom")
                }
                IconButton(onClick = { state.resetZoom() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Resetar Zoom")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (source is PdfSource.Url) {
                    IconButton(onClick = { platformHelper.download(source.url) }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Download")
                    }
                }
                IconButton(onClick = { platformHelper.share(source) }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Compartilhar")
                }
            }
        }
    }
}
