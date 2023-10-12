package base.storage.common.storage

import base.storage.common.cashe.CachePolicy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class LocalStorageTest {

    @Test
    fun `SHOULD get from network WHEN first time`(): Unit = runTest(timeout = 1.seconds) {
        // given
        val key: String = "key"
        val time: Long = 100L
        val cacheParams = mockk<CachePolicy>()
        val networkMock = mockk<suspend (String) -> String>()
        val localStorage: LocalStorage<String, String> = LocalStorage(
            maxElements = 1,
            cachePolicy = cacheParams,
            network = networkMock,
        )
        coEvery { networkMock.invoke(key) } returns "13"
        every { cacheParams.getTime() } returns time
        // when
        val list = localStorage.get(key).take(1).map { it.getOrThrow() }.toList()
        // then
        assertEquals(listOf("13"), list)
        coVerify(exactly = 1) { networkMock.invoke(key) }
    }

    @OptIn(FlowPreview::class)
    @Test
    fun `SHOULD get from network WHEN cache expired`() = runTest(timeout = 1.seconds) {
        // given
        val key: String = "key"
        val time: Long = 100L
        val cacheParams = mockk<CachePolicy>()
        val networkMock = mockk<suspend (String) -> String>()
        val localStorage: LocalStorage<String, String> = LocalStorage(
            maxElements = 1,
            cachePolicy = cacheParams,
            network = networkMock,
        )
        // when
        every { cacheParams.getTime() } returns time
        coEvery { networkMock.invoke(key) } returns "13"
        val list1 = localStorage.get(key).take(1).map { it.getOrThrow() }.toList()
        every { cacheParams.isExpired(eq(time)) } returns true
        coEvery { networkMock.invoke(key) } returns "15"
        val list2 = localStorage.get(key).take(2).map { it.getOrThrow() }.toList()
        // then
        assertEquals(listOf("13"), list1)
        assertEquals(listOf("13", "15"), list2)
        coVerify(exactly = 2) { networkMock.invoke(key) }
    }

    @Test
    fun `SHOULD get from cache WHEN cache not expired`(): Unit = runTest(timeout = 1.seconds) {
        // given
        val key: String = "key"
        val time: Long = 100L
        val cacheParams = mockk<CachePolicy>()
        val networkMock = mockk<suspend (String) -> String>()
        val localStorage: LocalStorage<String, String> = LocalStorage(
            maxElements = 1,
            cachePolicy = cacheParams,
            network = networkMock,
        )
        every { cacheParams.getTime() } returns time
        // when
        coEvery { networkMock.invoke(key) } returns "13"
        val list1 = localStorage.get(key).take(1).map { it.getOrThrow() }.toList()
        val list2 = localStorage.get(key).take(1).map { it.getOrThrow() }.toList()
        // then
        assertEquals(listOf("13"), list1)
        assertEquals(listOf("13"), list2)
        coVerify(exactly = 1) { networkMock.invoke(key) }
    }

    @Test
    fun `SHOULD get data db and network`(): Unit = runTest(timeout = 1.seconds) {
        // given
        val key: String = "key"
        val time: Long = 100L
        val cacheParams = mockk<CachePolicy>()
        val networkMock = mockk<suspend (String) -> String>()
        val permanentStorageMock = mockk<PermanentStorage<String, String>>()
        val localStorage: LocalStorage<String, String> = LocalStorage(
            maxElements = 1,
            cachePolicy = cacheParams,
            network = networkMock,
            permanentStorage = permanentStorageMock,
        )
        // when
        every { cacheParams.getTime() } returns time
        coEvery { networkMock.invoke(key) } returns "13"
        coEvery { permanentStorageMock.insertOrUpdate(key, "13") } returns Unit
        coEvery { permanentStorageMock.read(key) } returns "112"
        val actual = localStorage.get(key).take(2).map { it.getOrThrow() }.toList()
        // then
        val expected = listOf("112", "13")
        assertEquals(expected, actual)
        coVerify(exactly = 1) { networkMock.invoke(key) }
        coVerify(exactly = 1) { permanentStorageMock.read(key) }
        coVerify(exactly = 1) { permanentStorageMock.insertOrUpdate(key, "13") }
    }

    @Test
    fun `SHOULD from network WHEN cache expired AND has permanent`(): Unit =
        runTest(timeout = 1.seconds) {
            // given
            val key: String = "key"
            val time: Long = 100L
            val cacheParams = mockk<CachePolicy>()
            val networkMock = mockk<suspend (String) -> String>()
            val permanentStorageMock = mockk<PermanentStorage<String, String>>()
            val localStorage: LocalStorage<String, String> = LocalStorage(
                maxElements = 1,
                cachePolicy = cacheParams,
                network = networkMock,
                permanentStorage = permanentStorageMock,
            )
            // when
            every { cacheParams.getTime() } returns time
            coEvery { networkMock.invoke(key) } returns "13"
            coEvery { permanentStorageMock.insertOrUpdate(key, "13") } returns Unit
            coEvery { permanentStorageMock.read(key) } returns "112"
            val list1 = localStorage.get(key).take(2).map { it.getOrThrow() }.toList()
            every { cacheParams.isExpired(eq(time)) } returns true
            coEvery { networkMock.invoke(key) } returns "15"
            coEvery { permanentStorageMock.insertOrUpdate(key, "15") } returns Unit
            val list2 = localStorage.get(key).take(2).map { it.getOrThrow() }.toList()
            // then
            assertEquals(listOf("112", "13"), list1)
            assertEquals(listOf("13", "15"), list2)
            coVerify(exactly = 2) { networkMock.invoke(key) }
            coVerify(exactly = 1) { permanentStorageMock.read(key) }
            coVerify(exactly = 1) { permanentStorageMock.insertOrUpdate(key, "13") }
            coVerify(exactly = 1) { permanentStorageMock.insertOrUpdate(key, "15") }
        }

    //
    @Test
    fun `SHOULD update value`(): Unit = runTest(timeout = 1.seconds) {
        // given
        val key: String = "key"
        val time: Long = 100L
        val cacheParams = mockk<CachePolicy>()
        val networkMock = mockk<suspend (String) -> String>()
        val localStorage: LocalStorage<String, String> = LocalStorage(
            maxElements = 1,
            cachePolicy = cacheParams,
            network = networkMock,
        )
        // when
        every { cacheParams.getTime() } returns time
        coEvery { networkMock.invoke(key) } returns "13"
        every { cacheParams.isExpired(time) } returns false
        val list1 = localStorage.get(key).take(1).map { it.getOrThrow() }.toList()
        val list2 = mutableListOf<String>()
        val mutex = Mutex()
        val differ = CoroutineScope(Dispatchers.IO).async {
            localStorage.get(key)
                .take(3)
                .collect {
                    list2.add(it.getOrThrow())
                    if (mutex.isLocked) mutex.unlock()
                }
        }
        mutex.lock()
        mutex.withLock {
            localStorage.update(key) { "16" }
        }
        mutex.lock()
        mutex.withLock {
            localStorage.update(key) { "18" }
        }
        differ.await()
        // then
        Assert.assertEquals("13", list1[0])
        Assert.assertEquals("13", list2[0])
        Assert.assertEquals("16", list2[1])
        Assert.assertEquals("18", list2[2])
    }
}
