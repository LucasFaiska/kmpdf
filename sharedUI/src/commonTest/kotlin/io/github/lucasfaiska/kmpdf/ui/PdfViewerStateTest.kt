package io.github.lucasfaiska.kmpdf.ui

import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.repository.PdfRepository
import io.github.lucasfaiska.kmpdf.ui.cache.PdfPageCacheImpl
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

    private class MockPdfDocument : PdfDocument {
        override val pageCount: Int = 5

        override fun getPage(index: Int) = throw NotImplementedError()

        override fun close() {}
    }

    private class MockPdfRepository : PdfRepository {
        var loadCalled = false
        var shouldFail = false

        override suspend fun loadDocument(source: PdfSource): PdfDocument {
            loadCalled = true
            if (shouldFail) throw Exception("Load Error")
            return MockPdfDocument()
        }
    }

    @Test
    fun `given a new state when created then it should be empty`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), testScope)

        assertNull(state.document)
        assertFalse(state.loading)
        assertNull(state.error)
    }

    @Test
    fun `given a source when loading successfully then it should update document and status`() =
        testScope.runTest {
            val repository = MockPdfRepository()
            val state = PdfViewerState(PdfPageCacheImpl(5), this)

            state.load(PdfSource.Local("test"), repository)
            assertTrue(state.loading)
            advanceUntilIdle()

            assertFalse(state.loading)
            assertNotNull(state.document)
            assertEquals(5, state.document?.pageCount)
            assertTrue(repository.loadCalled)
        }

    @Test
    fun `given a failing repository when loading then it should handle error`() =
        testScope.runTest {
            val repository = MockPdfRepository().apply { shouldFail = true }
            val state = PdfViewerState(PdfPageCacheImpl(5), this)

            state.load(PdfSource.Url("http://error.com"), repository)
            advanceUntilIdle()

            assertFalse(state.loading)
            assertNull(state.document)
            assertNotNull(state.error)
            assertEquals("Load Error", state.error?.message)
        }
}
