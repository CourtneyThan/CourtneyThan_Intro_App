# 0003 – Offline-first content

**Status:** Accepted

## Context

A portfolio app is often opened once, by a recruiter, possibly on bad Wi-Fi. A loading spinner or an error screen is the worst first impression.

## Decision

`OfflineFirstProfileRepository` uses stale-while-revalidate:

1. Emit cached JSON immediately, or the JSON bundled in the APK when no cache exists (or the cache is corrupt).
2. Fetch the remote JSON in the background.
3. Only if it **parses** and is **different**: write it to the cache and emit it.

Network errors are swallowed; cancellation is always rethrown. The cache is written to a temp file and renamed, so a crash mid-write can't leave a half file.

## Consequences

- The app always shows content on the first frame after launch, even in airplane mode.
- A broken deploy of the remote JSON can never be cached, so it can't break the app.
- Room was considered and rejected: the content is one small document read as a whole, so a file is simpler and has no schema migrations. If content became relational (e.g. paginated projects), Room would be the next step.
- Every branch is unit tested with fakes in `OfflineFirstProfileRepositoryTest`.
