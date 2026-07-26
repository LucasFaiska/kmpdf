package io.github.lucasfaiska.kmpdf.ui.material3

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.ui.PdfViewerState
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Material3PdfUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given loading content when displayed then it should show circular progress indicator`() {
        composeTestRule.setContent {
            Material3LoadingContent()
        }
    }

    @Test
    fun `given password dialog when displayed then it should show labels and text field`() {
        val labels =
            Material3PasswordDialogLabels(
                title = "Locked",
                confirmButton = "GO",
            )

        composeTestRule.setContent {
            Material3PasswordDialog(
                isInvalid = false,
                onConfirm = {},
                labels = labels,
            )
        }

        composeTestRule.onNodeWithText("Locked").assertIsDisplayed()
        composeTestRule.onNodeWithText("GO").assertIsDisplayed()
    }

    @Test
    fun `given password dialog when text is empty then confirm button should be disabled`() {
        val labels = Material3PasswordDialogLabels(confirmButton = "GO")

        composeTestRule.setContent {
            Material3PasswordDialog(
                isInvalid = false,
                onConfirm = {},
                labels = labels,
            )
        }

        composeTestRule.onNodeWithText("GO").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Password").performTextInput("123")
        composeTestRule.onNodeWithText("GO").assertIsEnabled()
    }

    @Test
    fun `given password dialog when confirm clicked then it should trigger callback`() {
        var confirmedPassword = ""
        val labels = Material3PasswordDialogLabels(confirmButton = "GO")

        composeTestRule.setContent {
            Material3PasswordDialog(
                isInvalid = false,
                onConfirm = { confirmedPassword = it },
                labels = labels,
            )
        }

        composeTestRule.onNodeWithText("Password").performTextInput("secret")
        composeTestRule.onNodeWithText("GO").performClick()

        assert(confirmedPassword == "secret")
    }

    @Test
    fun `given toolbar when displayed then it should show navigation and zoom buttons`() {
        val state = mockk<PdfViewerState>(relaxed = true)
        every { state.currentPage } returns 1
        every { state.pageCount } returns 10

        val source = PdfSource.Url("test.pdf")
        val labels =
            Material3PdfToolbarLabels(
                zoomIn = "Plus",
                share = "ShareIt",
            )

        composeTestRule.setContent {
            Material3PdfToolbar(
                state = state,
                source = source,
                labels = labels,
            )
        }
    }
}
