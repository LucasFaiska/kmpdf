package io.github.lucasfaiska.kmpdf.ui.material3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfViewerState
import io.github.lucasfaiska.kmpdf.ui.rememberPdfPlatformActions

/**
 * Material 3 implementation of the PDF toolbar.
 *
 * @param state The state object for the PDF viewer.
 * @param source The source of the PDF document.
 * @param modifier The modifier to be applied to the layout.
 * @param labels The labels to be used for accessibility and text.
 */
@Composable
fun Material3PdfToolbar(
    state: PdfViewerState,
    source: PdfSource,
    modifier: Modifier = Modifier,
    labels: PdfToolbarLabels = PdfToolbarLabels(),
) {
    val platformActions = rememberPdfPlatformActions()

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
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = labels.previousPage)
                }
                Text(
                    text = "${state.currentPage} / ${state.pageCount}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = { state.scrollToPage(state.currentPage) }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = labels.nextPage)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { state.zoomOut() }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = labels.zoomOut)
                }
                Text(
                    text = "${(state.zoomScale * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.widthIn(min = 40.dp),
                )
                IconButton(onClick = { state.zoomIn() }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = labels.zoomIn)
                }
                IconButton(onClick = { state.resetZoom() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = labels.resetZoom)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (source is PdfSource.Url) {
                    IconButton(onClick = { platformActions.download(source.url) }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = labels.download)
                    }
                }
                IconButton(onClick = { platformActions.share(source) }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = labels.share)
                }
            }
        }
    }
}
