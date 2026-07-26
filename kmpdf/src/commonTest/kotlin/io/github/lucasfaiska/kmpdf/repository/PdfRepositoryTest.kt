package io.github.lucasfaiska.kmpdf.repository

import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.reader.PdfReader
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PdfRepositoryTest {
    private class MockPdfLoader : PdfLoader {
        var lastSource: PdfSource? = null
        var resultBytes: ByteArray = byteArrayOf()
        var shouldThrow: Boolean = false

        override suspend fun load(source: PdfSource): ByteArray {
            if (shouldThrow) throw Exception("Load failed")
            lastSource = source
            return resultBytes
        }
    }

    private class MockPdfReader : PdfReader {
        var lastBytes: ByteArray? = null
        var lastPassword: String? = null
        var resultStatus: PdfLoadStatus =
            PdfLoadStatus.Success(
                object : PdfDocument {
                    override val pageCount: Int = 0

                    override fun getPage(index: Int) = throw NotImplementedError()

                    override fun close() {}
                },
            )

        override suspend fun open(
            bytes: ByteArray,
            password: String?,
        ): PdfLoadStatus {
            lastBytes = bytes
            lastPassword = password
            return resultStatus
        }
    }

    @Test
    fun `given valid source and password when loading document then it should return success status`() =
        runTest {
            val loader = MockPdfLoader()
            val reader = MockPdfReader()
            val repository = PdfRepositoryImpl(loader, reader)
            val source = PdfSource.Local("test.pdf")
            val password = "password123"
            val expectedBytes = byteArrayOf(1, 2, 3)
            loader.resultBytes = expectedBytes

            val result = repository.loadDocument(source, password)

            assertEquals(source, loader.lastSource)
            assertEquals(expectedBytes, reader.lastBytes)
            assertEquals(password, reader.lastPassword)
            assertEquals(reader.resultStatus, result)
        }

    @Test
    fun `given reader returns password required when loading then it should return password required status`() =
        runTest {
            val loader = MockPdfLoader()
            val reader = MockPdfReader()
            reader.resultStatus = PdfLoadStatus.PasswordRequired
            val repository = PdfRepositoryImpl(loader, reader)

            val result = repository.loadDocument(PdfSource.Local("test"))

            assertEquals(PdfLoadStatus.PasswordRequired, result)
        }

    @Test
    fun `given reader returns invalid password when loading then it should return invalid password status`() =
        runTest {
            val loader = MockPdfLoader()
            val reader = MockPdfReader()
            reader.resultStatus = PdfLoadStatus.InvalidPassword
            val repository = PdfRepositoryImpl(loader, reader)

            val result = repository.loadDocument(PdfSource.Local("test"), "wrong")

            assertEquals(PdfLoadStatus.InvalidPassword, result)
        }

    @Test
    fun `given reader returns error when loading then it should return error status`() =
        runTest {
            val loader = MockPdfLoader()
            val reader = MockPdfReader()
            val error = PdfError(PdfErrorType.CORRUPTED)
            reader.resultStatus = PdfLoadStatus.Error(error)
            val repository = PdfRepositoryImpl(loader, reader)

            val result = repository.loadDocument(PdfSource.Local("test"))

            assertTrue(result is PdfLoadStatus.Error)
            assertEquals(error, result.error)
        }

    @Test
    fun `given loader throws exception when loading document then it should return generic error status`() =
        runTest {
            val loader = MockPdfLoader()
            val reader = MockPdfReader()
            val repository = PdfRepositoryImpl(loader, reader)
            val source = PdfSource.Local("test.pdf")
            loader.shouldThrow = true

            val result = repository.loadDocument(source, null)

            assertTrue(result is PdfLoadStatus.Error)
            assertEquals(PdfErrorType.GENERIC, result.error.type)
            assertEquals("Load failed", result.error.message)
        }

    @Test
    fun `given invalid password and password required states when instantiated then they should exist`() {
        val invalidPassword = PdfLoadStatus.InvalidPassword
        val passwordRequired = PdfLoadStatus.PasswordRequired

        assertNotNull(invalidPassword)
        assertNotNull(passwordRequired)
    }
}
