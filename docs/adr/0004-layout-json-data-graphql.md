# 0004 – Layout from JSON, data from GraphQL

**Status:** Accepted

## Context

So far `profile.json` held both the layout (which sections, in what order) and all the data inside them. Real SDUI systems usually split these: the layout response says *what goes where*, and some sections fetch their own live data, often over GraphQL. This app should show that pattern, while keeping `profile.json` as the single source of layout.

## Decision

- A new `places` section type. The JSON places it and lists ISO country codes (`"countryCodes": ["JP", "IT"]`).
- The details for those countries (name, flag, capital, currency, continent, languages) come from the public [Countries GraphQL API](https://countries.trevorblades.com) through **Apollo Kotlin 4**:
  - `src/main/graphql/schema.graphqls`: the part of the API's schema the app uses.
  - `src/main/graphql/Countries.graphql`: the one query. Apollo generates the type-safe `CountriesQuery` at build time, so a typo in a field name fails the build, not the app.
  - `src/main/graphql/extra.graphqls`: `@typePolicy(keyFields: "code")`, so the normalized cache stores each country once under its ISO code.
- **Caching:** a memory cache chained to a SQLite cache, read with `FetchPolicy.CacheAndNetwork`. Cached places show instantly and survive restarts offline; the network then refreshes them. This mirrors the stale-while-revalidate design of the profile content (ADR 0003).
- **Boundaries:** `PlacesRepository` maps Apollo's generated classes to the app's own `Place` model, so the UI never depends on generated code. Each places section gets its own `PlacesViewModel` (keyed by section id), fed the codes from the JSON.

## Consequences

- The layout can add, remove or reorder countries without an app release, and the data stays fresh without editing JSON.
- A network failure only affects this section: the rest of the screen is local content. The section shows skeletons while loading, and an error card with Retry when there is no cache and no network.
- Tests run the real Apollo client against `MockWebServer`, covering the generated query, variables, parsing, cache and mapping together.
- Trade-off: a second network dependency. A production app would put both behind its own backend (one GraphQL endpoint serving layout and data), which this structure is ready for.
