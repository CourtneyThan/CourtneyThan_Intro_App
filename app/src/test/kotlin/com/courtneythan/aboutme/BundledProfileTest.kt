package com.courtneythan.aboutme

import com.courtneythan.aboutme.core.model.Action
import com.courtneythan.aboutme.core.model.ProfileParser
import com.courtneythan.aboutme.core.model.Section
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the content file: if a typo in profile.json would make a section disappear
 * or a button point nowhere, CI fails instead of the bug reaching the app.
 */
class BundledProfileTest {

    private val profile = ProfileParser().parse(File("src/main/assets/profile.json").readText())

    @Test
    fun `welcome screen is present`() {
        assertNotNull(profile.welcome)
    }

    @Test
    fun `every bundled section is supported`() {
        val unsupported = profile.tabs.flatMap { it.sections }.filterIsInstance<Section.Unsupported>()
        assertTrue("Unsupported sections in profile.json: $unsupported", unsupported.isEmpty())
    }

    @Test
    fun `tab ids and section ids are unique`() {
        val tabIds = profile.tabs.map { it.id }
        assertEquals("Duplicate tab ids", tabIds.size, tabIds.toSet().size)
        profile.tabs.forEach { tab ->
            val ids = tab.sections.map { it.id }
            assertEquals("Duplicate section ids in ${tab.id} break LazyColumn keys", ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `home tab opens with the hero`() {
        assertTrue(profile.tabs.first().sections.first() is Section.Hero)
    }

    @Test
    fun `every open_tab action points to a real tab`() {
        val tabIds = profile.tabs.map { it.id }.toSet()
        val targets = profile.tabs
            .flatMap { it.sections }
            .filterIsInstance<Section.LinkList>()
            .flatMap { it.links }
            .mapNotNull { (it.action as? Action.OpenTab)?.tabId }
        assertTrue("Unknown tab ids: ${targets - tabIds}", tabIds.containsAll(targets))
    }

    @Test
    fun `places use two-letter ISO country codes`() {
        val codes = profile.tabs.flatMap { it.sections }.filterIsInstance<Section.Places>().flatMap { it.countryCodes }
        val bad = codes.filterNot { it.matches(Regex("[A-Z]{2}")) }
        assertTrue("Country codes must look like \"JP\": $bad", bad.isEmpty())
    }
}
