package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.github.lucasfaiska.kmpdf.ui.cache.PdfPageCacheImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfViewerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given loading state when viewing then it should show loading content slot`() {
        val mockState = mockk<PdfViewerState>(relaxed = true)
        every { mockState.loading } returns true
        every { mockState.error } returns null
        every { mockState.document } returns null

        composeTestRule.setContent {
            PdfViewer(
                state = mockState,
                loadingContent = { BasicText("LOAD") },
            )
        }

        composeTestRule.onNodeWithText("LOAD").assertIsDisplayed()
    }

    @Test
    fun `given error state when viewing then it should show error content slot`() {
        val mockState = mockk<PdfViewerState>(relaxed = true)
        every { mockState.loading } returns false
        every { mockState.error } returns PdfError(PdfErrorType.GENERIC, "FAIL")

        composeTestRule.setContent {
            PdfViewer(
                state = mockState,
                errorContent = { BasicText(it.message ?: "") },
            )
        }

        composeTestRule.onNodeWithText("FAIL").assertIsDisplayed()
    }

    @Test
    fun `given getPage called when dimensions are zero then it should return null`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), TestScope())
        var isNull = false
        composeTestRule.setContent {
            val bitmap = state.getPage(0, 0, 0)
            isNull = bitmap == null
        }

        assertTrue(isNull)
    }
}
