package io.github.lucasfaiska.kmpdf.ui

import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.reader.PdfReader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PdfViewerStateTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private class MockPdfLoader : PdfLoader {
        var loadCalled = false

        override suspend fun load(source: PdfSource): ByteArray {
            loadCalled = true
            return byteArrayOf(1, 2, 3)
        }
    }

    private class MockPdfDocument : PdfDocument {
        override val pageCount: Int = 5

        override fun getPage(index: Int) = throw NotImplementedError()

        override fun close() {}
    }

    private class MockPdfReader : PdfReader {
        override suspend fun open(bytes: ByteArray): PdfDocument = MockPdfDocument()
    }

    @Test
    fun `given a new state when created then it should be empty`() {
        // Given
        val state = PdfViewerState(MockPdfLoader(), MockPdfReader(), testScope)

        // Then
        assertNull(state.document)
        assertFalse(state.loading)
        assertNull(state.error)
    }

    @Test
    fun `given a source when loading successfully then it should update document and status`() =
        testScope.runTest {
            // Given
            val loader = MockPdfLoader()
            val state = PdfViewerState(loader, MockPdfReader(), this)

            // When
            state.load(PdfSource.Local("test"))

            // Then
            assertTrue(state.loading)

            // When
            advanceUntilIdle()

            // Then
            assertFalse(state.loading)
            assertNotNull(state.document)
            assertEquals(5, state.document?.pageCount)
            assertTrue(loader.loadCalled)
        }

    @Test
    fun `given a failing loader when loading then it should handle error`() =
        testScope.runTest {
            // Given
            val failingLoader =
                object : PdfLoader {
                    override suspend fun load(source: PdfSource): ByteArray = throw Exception("Network Error")
                }
            val state = PdfViewerState(failingLoader, MockPdfReader(), this)

            // When
            state.load(PdfSource.Url("http://error.com"))
            advanceUntilIdle()

            // Then
            assertFalse(state.loading)
            assertNull(state.document)
            assertNotNull(state.error)
            assertEquals("Network Error", state.error?.message)
        }
}
