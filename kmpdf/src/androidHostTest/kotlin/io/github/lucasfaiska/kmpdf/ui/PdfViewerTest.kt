package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.mockk.every
import io.mockk.mockk
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
    fun `given password required when viewing then it should show password dialog slot`() {
        val mockState = mockk<PdfViewerState>(relaxed = true)
        every { mockState.isPasswordRequired } returns true

        composeTestRule.setContent {
            PdfViewer(
                state = mockState,
                passwordDialog = { _, _ -> BasicText("LOCKED") },
            )
        }

        composeTestRule.onNodeWithText("LOCKED").assertIsDisplayed()
    }

    @Test
    fun `given loading document when viewing then it should not show bars`() {
        val mockState = mockk<PdfViewerState>(relaxed = true)
        every { mockState.loading } returns true

        composeTestRule.setContent {
            PdfViewer(
                state = mockState,
                topBar = { BasicText("TOP") },
                bottomBar = { BasicText("BOTTOM") },
            )
        }

        composeTestRule.onNodeWithText("TOP").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("BOTTOM").assertIsNotDisplayed()
    }

    @Test
    fun `given document loaded when viewing then it should show bars`() {
        val mockState = mockk<PdfViewerState>(relaxed = true)
        every { mockState.loading } returns false
        every { mockState.error } returns null
        every { mockState.document } returns mockk()

        composeTestRule.setContent {
            PdfViewer(
                state = mockState,
                topBar = { BasicText("TOP") },
                bottomBar = { BasicText("BOTTOM") },
            )
        }

        composeTestRule.onNodeWithText("TOP").assertIsDisplayed()
        composeTestRule.onNodeWithText("BOTTOM").assertIsDisplayed()
    }
}
