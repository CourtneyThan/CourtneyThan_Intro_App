package com.courtneythan.aboutme.feature.profile.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.courtneythan.aboutme.core.model.Action
import com.courtneythan.aboutme.core.model.Section

/**
 * The SDUI "registry": maps each server-defined [Section] to a Compose component.
 *
 * A `when` over a sealed class is used instead of a runtime Map<String, Composable>
 * so the compiler fails the build if a new section type has no renderer.
 * See docs/adr/0001-server-driven-ui.md.
 */
@Composable
fun SectionRenderer(
    section: Section,
    onAction: (Action) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Headers run edge to edge; everything else gets the 24dp page margin.
    val content = modifier.padding(horizontal = 24.dp)
    when (section) {
        is Section.Hero -> HeroSection(section, modifier)
        is Section.Header -> HeaderSection(section, onBack, modifier)
        is Section.About -> AboutSection(section, content)
        is Section.LinkList -> LinkListSection(section, onAction, content)
        is Section.Skills -> SkillsSection(section, content)
        is Section.Timeline -> TimelineSection(section, content)
        is Section.FunFacts -> FunFactsSection(section, content)
        is Section.Contact -> ContactSection(section, onAction, content)
        is Section.Unsupported -> Unit // filtered out upstream; render nothing just in case
    }
}
