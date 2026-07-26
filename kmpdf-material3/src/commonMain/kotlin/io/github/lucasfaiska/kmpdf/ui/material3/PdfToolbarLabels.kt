package io.github.lucasfaiska.kmpdf.ui.material3

import androidx.compose.runtime.Immutable

/**
 * Labels for the PDF toolbar components.
 *
 * @property previousPage Accessibility description for the previous page button.
 * @property nextPage Accessibility description for the next page button.
 * @property zoomIn Accessibility description for the zoom in button.
 * @property zoomOut Accessibility description for the zoom out button.
 * @property resetZoom Accessibility description for the reset zoom button.
 * @property share Accessibility description for the share button.
 * @property download Accessibility description for the download button.
 */
@Immutable
data class PdfToolbarLabels(
    val previousPage: String = "Previous Page",
    val nextPage: String = "Next Page",
    val zoomIn: String = "Zoom In",
    val zoomOut: String = "Zoom Out",
    val resetZoom: String = "Reset Zoom",
    val share: String = "Share",
    val download: String = "Download",
)
