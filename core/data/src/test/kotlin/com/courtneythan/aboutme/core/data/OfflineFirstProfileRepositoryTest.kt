package com.courtneythan.aboutme.core.data

import app.cash.turbine.test
import com.courtneythan.aboutme.core.model.ProfileParser
import com.courtneythan.aboutme.core.model.Section
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class OfflineFirstProfileRepositoryTest {

    private fun json(name: String) =
        """{ "tabs": [ { "id": "home", "label": "Home", "sections": [ { "type": "about", "id": "a", "title": "$name", "paragraphs": [] } ] } ] }"""

    private class FakeCache(var stored: String? = null) : ProfileCache {
        override suspend fun read() = stored
        override suspend fun write(json: String) { stored = json }
    }

    // Uses the runTest scheduler so flowOn() runs on virtual time.
    private fun TestScope.repository(
        bundled: String = json("bundled"),
        cache: FakeCache = FakeCache(),
        remote: RemoteProfileSource = RemoteProfileSource { null },
    ) = OfflineFirstProfileRepository(
        bundled = { bundled },
        cache = cache,
        remote = remote,
        parser = ProfileParser(),
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    private val ProfileSnapshot.title get() = (profile.tabs.single().sections.single() as Section.About).title

    @Test
    fun `first launch offline shows bundled content`() = runTest {
        repository().profile().test {
            val snapshot = awaitItem()
            assertEquals(ContentSource.BUNDLED, snapshot.source)
            assertEquals("bundled", snapshot.title)
            awaitComplete()
        }
    }

    @Test
    fun `cached content wins over bundled`() = runTest {
        repository(cache = FakeCache(json("cached"))).profile().test {
            assertEquals("cached", awaitItem().title)
            awaitComplete()
        }
    }

    @Test
    fun `new remote content is emitted and cached`() = runTest {
        val cache = FakeCache()
        repository(cache = cache, remote = { json("remote") }).profile().test {
            assertEquals(ContentSource.BUNDLED, awaitItem().source)
            val updated = awaitItem()
            assertEquals(ContentSource.REMOTE, updated.source)
            assertEquals("remote", updated.title)
            awaitComplete()
        }
        assertEquals(json("remote"), cache.stored)
    }

    @Test
    fun `identical remote content does not emit twice`() = runTest {
        repository(bundled = json("same"), remote = { json("same") }).profile().test {
            awaitItem()
            awaitComplete()
        }
    }

    @Test
    fun `network failure keeps local content`() = runTest {
        val cache = FakeCache()
        repository(cache = cache, remote = { throw IOException("offline") }).profile().test {
            assertEquals("bundled", awaitItem().title)
            awaitComplete()
        }
        assertNull(cache.stored)
    }

    @Test
    fun `broken remote json is never cached`() = runTest {
        val cache = FakeCache()
        repository(cache = cache, remote = { "{ not json" }).profile().test {
            awaitItem()
            awaitComplete()
        }
        assertNull(cache.stored)
    }

    @Test
    fun `corrupt cache falls back to bundled`() = runTest {
        repository(cache = FakeCache("garbage")).profile().test {
            assertEquals(ContentSource.BUNDLED, awaitItem().source)
            awaitComplete()
        }
    }
}
