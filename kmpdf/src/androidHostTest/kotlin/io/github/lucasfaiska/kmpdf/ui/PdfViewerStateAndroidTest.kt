package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
import io.github.lucasfaiska.kmpdf.model.PdfPage
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import io.github.lucasfaiska.kmpdf.ui.cache.PdfPageCacheImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfViewerStateAndroidTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given document loaded when calling getPage then it should trigger render and cache`() = runTest {
        val mockPage = mockk<PdfPage>(relaxed = true)
        val mockDocument = mockk<PdfDocument>(relaxed = true)
        val testBytes = ByteArray(10 * 10 * 4)

        every { mockDocument.pageCount } returns 1
        every { mockDocument.getPage(0) } returns mockPage
        coEvery { mockPage.render(10, 10) } returns testBytes

        val repository = mockk<PdfRepository>()
        coEvery { repository.loadDocument(any(), any()) } returns PdfLoadStatus.Success(mockDocument)

        val state = PdfViewerState(PdfPageCacheImpl(5), this)
        state.load(PdfSource.Local("test"), repository)
        advanceUntilIdle()

        var bitmap: ImageBitmap? = null
        composeTestRule.setContent {
            bitmap = state.getPage(0, 10, 10)
        }

        assertNull(bitmap)

        composeTestRule.waitForIdle()

        composeTestRule.setContent {
            bitmap = state.getPage(0, 10, 10)
        }
        assertNotNull(bitmap)
    }

    @Test
    fun `given invalid dimensions when calling getPage then it should return null`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), TestScope())
        var bitmap: ImageBitmap? = ImageBitmap(1, 1) // Just to check it changes to null
        
        composeTestRule.setContent {
            bitmap = state.getPage(0, 0, 10)
        }
        assertNull(bitmap)
        
        composeTestRule.setContent {
            bitmap = state.getPage(0, 10, -1)
        }
        assertNull(bitmap)
    }
}
