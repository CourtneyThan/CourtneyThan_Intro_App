package com.courtneythan.aboutme.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileParserTest {

    private val parser = ProfileParser()

    /** Wraps sections in a minimal one-tab document. */
    private fun doc(sections: String, extra: String = "") =
        """{ "schemaVersion": 1 $extra, "tabs": [ { "id": "home", "label": "Home", "icon": "home", "sections": [ $sections ] } ] }"""

    private fun sectionsOf(json: String) = parser.parse(json).tabs.single().sections

    @Test
    fun `parses tabs and known section types in order`() {
        val profile = parser.parse(
            doc(
                """
                { "type": "hero", "id": "h", "name": "Courtney", "title": "Android",
                  "stats": [ { "value": "3", "label": "years" } ] },
                { "type": "about", "id": "a", "title": "About", "paragraphs": ["one"] },
                { "type": "link_list", "id": "l", "links": [
                    { "title": "Toolbox", "subtitle": "s", "action": { "type": "open_tab", "tabId": "toolbox" } } ] }
                """,
            ),
        )

        val tab = profile.tabs.single()
        assertEquals("home", tab.id)
        assertEquals(listOf("h", "a", "l"), tab.sections.map { it.id })
        assertEquals(listOf(Stat("3", "years")), (tab.sections[0] as Section.Hero).stats)
        val link = (tab.sections[2] as Section.LinkList).links.single()
        assertEquals(Action.OpenTab("toolbox"), link.action)
        assertEquals("star", link.icon) // default applied
    }

    @Test
    fun `places section keeps its country codes`() {
        val section = sectionsOf(
            doc("""{ "type": "places", "id": "p", "title": "Travel", "countryCodes": ["JP", "IT"] }"""),
        ).single()
        assertEquals(Section.Places(id = "p", title = "Travel", countryCodes = listOf("JP", "IT")), section)
    }

    @Test
    fun `welcome is decoded when present`() {
        val profile = parser.parse(
            doc("", extra = """, "welcome": { "greeting": "Hi", "bubble": "b", "title": "T", "body": "x", "cta": "Go" }"""),
        )
        assertEquals("Go", profile.welcome?.cta)
        assertEquals("Skip", profile.welcome?.skip)
    }

    @Test
    fun `broken welcome is dropped instead of failing the document`() {
        val profile = parser.parse(doc("", extra = """, "welcome": { "greeting": "Hi" }"""))
        assertNull(profile.welcome)
        assertNotNull(profile.tabs.single())
    }

    @Test
    fun `unknown section type becomes Unsupported and the rest still parse`() {
        val sections = sectionsOf(
            doc(
                """
                { "type": "video_reel", "id": "v", "url": "https://x" },
                { "type": "about", "id": "a", "title": "About", "paragraphs": [] }
                """,
            ),
        )
        assertEquals(Section.Unsupported(id = "v", originalType = "video_reel"), sections[0])
        assertTrue(sections[1] is Section.About)
    }

    @Test
    fun `unknown action type makes only that section Unsupported`() {
        val sections = sectionsOf(
            doc(
                """
                { "type": "link_list", "id": "l", "links": [
                    { "title": "t", "subtitle": "s", "action": { "type": "open_camera" } } ] },
                { "type": "header", "id": "hd", "title": "Hi" }
                """,
            ),
        )
        assertEquals(Section.Unsupported(id = "l", originalType = "link_list"), sections[0])
        assertTrue(sections[1] is Section.Header)
    }

    @Test
    fun `known type with missing required field becomes Unsupported`() {
        val sections = sectionsOf(doc("""{ "type": "hero", "id": "h", "name": "No title" }"""))
        assertEquals(Section.Unsupported(id = "h", originalType = "hero"), sections.single())
    }

    @Test
    fun `section without id or type gets a stable fallback id`() {
        val sections = sectionsOf(doc("""{ "hello": "world" }"""))
        assertEquals(Section.Unsupported(id = "section-0", originalType = "<missing>"), sections.single())
    }

    @Test
    fun `unknown fields are ignored so the server can add data freely`() {
        val sections = sectionsOf(
            doc("""{ "type": "about", "id": "a", "title": "About", "paragraphs": [], "accent": "pink" }""", extra = """, "theme": "dark" """),
        )
        assertTrue(sections.single() is Section.About)
    }

    @Test(expected = UnsupportedSchemaException::class)
    fun `newer schema version is rejected`() {
        parser.parse("""{ "schemaVersion": 99, "tabs": [] }""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `malformed json throws`() {
        parser.parse("""{ "tabs": [ """)
    }
}
