package com.courtneythan.aboutme.feature.profile

import com.courtneythan.aboutme.core.model.Action
import com.courtneythan.aboutme.core.model.ContactLink
import com.courtneythan.aboutme.core.model.FunFact
import com.courtneythan.aboutme.core.model.LinkItem
import com.courtneythan.aboutme.core.model.Profile
import com.courtneythan.aboutme.core.model.Section
import com.courtneythan.aboutme.core.model.SkillCard
import com.courtneythan.aboutme.core.model.Stat
import com.courtneythan.aboutme.core.model.Tab
import com.courtneythan.aboutme.core.model.TimelineEntry
import com.courtneythan.aboutme.core.model.Welcome

/** Small, fixed content for @Preview functions and UI tests. */
internal object PreviewData {
    val profile = Profile(
        schemaVersion = 1,
        welcome = Welcome(
            greeting = "Hello there!",
            bubble = "Psst… want to meet me?",
            title = "MEET COURTNEY",
            body = "The Android engineer behind this little app.",
            cta = "Let's go",
        ),
        tabs = listOf(
            Tab(
                id = "home",
                label = "Home",
                icon = "home",
                sections = listOf(
                    Section.Hero(
                        id = "hero",
                        name = "Courtney Than",
                        nickname = "Thien",
                        title = "Android Engineer",
                        stats = listOf(Stat("3", "years at Expedia"), Stat("+15%", "AI rec engagement")),
                    ),
                    Section.About(id = "about", title = "About me", paragraphs = listOf("I build friendly Android screens.")),
                    Section.LinkList(
                        id = "links",
                        links = listOf(LinkItem("My toolbox", "Kotlin & Compose", "star", Action.OpenTab("toolbox"))),
                    ),
                ),
            ),
            Tab(
                id = "toolbox",
                label = "Toolbox",
                icon = "star",
                sections = listOf(
                    Section.Header(id = "header", title = "My toolbox"),
                    Section.Skills(
                        id = "skills",
                        cards = listOf(
                            SkillCard("K", "Kotlin", "Every day", "Android"),
                            SkillCard("UI", "Jetpack Compose", "Reusable UI", "Android", featured = true),
                            SkillCard("OK", "Unit tests", "JUnit & Compose UI", "Testing"),
                        ),
                    ),
                ),
            ),
            Tab(
                id = "journey",
                label = "Journey",
                icon = "map",
                sections = listOf(
                    Section.Header(id = "header", title = "My journey"),
                    Section.Timeline(
                        id = "timeline",
                        entries = listOf(
                            TimelineEntry("Software Engineer II", "Expedia Group", "2025 – now", listOf("Built SDUI experiences"), current = true),
                            TimelineEntry("Software Engineer I", "Expedia Group", "2023 – 2025", listOf("Help Center lead"), badge = "Employee of the Month"),
                        ),
                    ),
                ),
            ),
            Tab(
                id = "fun",
                label = "Fun facts",
                icon = "heart",
                sections = listOf(
                    Section.Header(id = "header", title = "Fun facts", subtitle = "Tap a card to flip it", showBack = false),
                    Section.FunFacts(id = "facts", facts = listOf(FunFact("Guess my award?", "Employee of the Month!"))),
                    Section.Contact(
                        id = "contact",
                        title = "Say hi",
                        message = "I'd love to chat.",
                        links = listOf(ContactLink("email", "Email me", "mailto:hello@example.com")),
                    ),
                ),
            ),
        ),
    )
}
