package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.ui.test.junit4.createComposeRule
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfDefaultsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given a composable context when remembering pdf repository then it should return non-null instance`() {
        var repository: PdfRepository? = null
        composeTestRule.setContent {
            repository = rememberPdfRepository()
        }

        assertNotNull(repository)
    }

    @Test
    fun `given a composable context when remembering platform actions then it should return non-null instance`() {
        var result: PdfPlatformActions? = null
        composeTestRule.setContent {
            result = rememberPdfPlatformActions()
        }

        assertNotNull(result)
        assertTrue(result is AndroidPdfPlatformActions)
    }
}
