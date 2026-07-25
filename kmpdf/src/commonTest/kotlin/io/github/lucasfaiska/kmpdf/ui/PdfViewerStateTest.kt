package io.github.lucasfaiska.kmpdf.ui

import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
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
        var requiredPassword = false
        var lastPasswordUsed: String? = null

        override suspend fun loadDocument(
            source: PdfSource,
            password: String?,
        ): PdfLoadStatus {
            loadCalled = true
            lastPasswordUsed = password
            if (shouldFail) return PdfLoadStatus.Error(PdfError(PdfErrorType.GENERIC))
            if (requiredPassword && password == null) return PdfLoadStatus.PasswordRequired
            if (requiredPassword && password != "correct") return PdfLoadStatus.InvalidPassword
            return PdfLoadStatus.Success(MockPdfDocument())
        }
    }

    @Test
    fun `given a new state when created then it should be empty and have default zoom`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), testScope)

        assertNull(state.document)
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(1f, state.zoomScale)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `given state when zooming in then zoomScale should increase`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), testScope)

        state.zoomIn()
        assertEquals(1.25f, state.zoomScale)

        state.zoomIn()
        assertEquals(1.5f, state.zoomScale)
    }

    @Test
    fun `given state when zooming out then zoomScale should decrease but not below 1`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), testScope)

        state.zoomIn()
        state.zoomIn()
        assertEquals(1.5f, state.zoomScale)

        state.zoomOut()
        assertEquals(1.25f, state.zoomScale)

        state.zoomOut()
        assertEquals(1.0f, state.zoomScale)

        state.zoomOut()
        assertEquals(1.0f, state.zoomScale)
    }

    @Test
    fun `given state when resetting zoom then zoomScale should be 1`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), testScope)

        state.zoomIn()
        state.zoomIn()
        state.resetZoom()

        assertEquals(1.0f, state.zoomScale)
    }

    @Test
    fun `given state when updating zoom with scale then zoomScale should multiply`() {
        val state = PdfViewerState(PdfPageCacheImpl(5), testScope)

        state.updateZoom(2.0f)
        assertEquals(2.0f, state.zoomScale)

        state.updateZoom(0.5f)
        assertEquals(1.0f, state.zoomScale)
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
            assertEquals(PdfErrorType.GENERIC, state.error?.type)
        }

    @Test
    fun `given a protected document when loading without password then it should require password`() =
        testScope.runTest {
            val repository = MockPdfRepository().apply { requiredPassword = true }
            val state = PdfViewerState(PdfPageCacheImpl(5), this)

            state.load(PdfSource.Local("protected"), repository)
            advanceUntilIdle()

            assertTrue(state.isPasswordRequired)
            assertFalse(state.isPasswordInvalid)
            assertNull(state.document)
        }

    @Test
    fun `given a protected document when loading with incorrect password then it should flag invalid password`() =
        testScope.runTest {
            val repository = MockPdfRepository().apply { requiredPassword = true }
            val state = PdfViewerState(PdfPageCacheImpl(5), this)

            state.load(PdfSource.Local("protected"), repository, "wrong")
            advanceUntilIdle()

            assertFalse(state.isPasswordRequired)
            assertTrue(state.isPasswordInvalid)
            assertNull(state.document)
        }

    @Test
    fun `given a protected document when unlocking with correct password then it should load document`() =
        testScope.runTest {
            val repository = MockPdfRepository().apply { requiredPassword = true }
            val state = PdfViewerState(PdfPageCacheImpl(5), this)

            state.load(PdfSource.Local("protected"), repository)
            advanceUntilIdle()
            assertTrue(state.isPasswordRequired)

            state.unlock("correct", repository)
            advanceUntilIdle()

            assertFalse(state.isPasswordRequired)
            assertFalse(state.isPasswordInvalid)
            assertNotNull(state.document)
            assertEquals("correct", repository.lastPasswordUsed)
        }
}
