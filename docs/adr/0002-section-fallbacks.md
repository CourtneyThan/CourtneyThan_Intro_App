# 0002 – Fallbacks for unknown and broken sections

**Status:** Accepted

## Context

With server-driven UI, the server and the app version on a user's phone change on different schedules. The server may send a section type an older app has never heard of, or a section with a missing field. If one bad section throws during parsing, the whole screen fails.

## Decision

- Decode the top level loosely (`sections: List<JsonObject>`), then decode **each section separately**.
- If a section fails to decode, replace it with `Section.Unsupported(id, originalType)`. The ViewModel filters these out; the renderer draws nothing for them.
- `ignoreUnknownKeys = true`, so adding fields is always safe.
- A `schemaVersion` higher than the app supports is a breaking change: the parser throws, and the repository keeps showing the last good content instead.

## Consequences

- One bad block never takes down the screen. This is tested in `ProfileParserTest`.
- Silent skipping can hide mistakes, so `BundledProfileTest` fails CI if the bundled `profile.json` contains any unsupported section.
- In a production app, `Unsupported` would also be logged to analytics so the team can see how often old clients meet new types.
