# 0001 – Server-driven UI with a sealed class registry

**Status:** Accepted

## Context

The app's content (sections, their order, their text) should be changeable without shipping a new build. That's the core idea of server-driven UI: the server describes *what* to show, and the client knows *how* to draw each building block.

There are two common ways to map a server type to a component:

1. **Runtime registry:** a `Map<String, @Composable (JsonObject) -> Unit>`. Flexible, but each component parses its own JSON, mistakes are found at runtime, and nothing tells you a type has no renderer.
2. **Typed model + exhaustive `when`:** decode JSON into a `sealed class Section`, then render with `when (section)`.

## Decision

Use option 2. `Section` is a sealed class decoded with kotlinx.serialization (class discriminator `"type"`), and `SectionRenderer` uses an exhaustive `when`.

The document is organised as **tabs → sections**, and taps are described as **actions** (`open_tab`, `open_url`), also sealed classes. So the server controls not just what's on a screen, but which tabs exist and where buttons go.

## Consequences

- Adding a section type is: add a subclass → the compiler points at every `when` that must handle it → add a composable. No forgotten renderers.
- Components receive typed data (`Section.Hero`), not raw JSON, which keeps them simple and previewable.
- The model lives in a pure Kotlin module (`:core:model`), so parsing is tested on the JVM in milliseconds.
- Trade-off: a brand-new component type still needs an app release. Old app versions handle new types safely (see ADR 0002).
