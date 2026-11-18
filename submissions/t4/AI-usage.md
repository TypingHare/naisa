# AI Usage

## Vincent Lai

### Nov. 15-18, 2026: Database seeders

Used Codex (Astra-xhigh) to review
[PR #92](https://github.com/TypingHare/naisa/pull/92) and complete the remaining
database seeders for [issue #89](https://github.com/TypingHare/naisa/issues/89).
I instructed Codex to follow the existing client and venue seeder format. Codex
generated seeders for sports, venue sports, opening intervals, reviews, and
crowd reports, using `DatabaseSeeder<T>`, separate
`generateEntities(seed, count)` and `seed(entities)` methods, reproducible Faker
data, dependency-ordered priorities, and repository insertion with logging.
Generated Java files include `Vincent Lai, Codex` author attribution. I
committed the implementation and opened
[PR #93](https://github.com/TypingHare/naisa/pull/93).

Codex checked foreign-key relationships, distinct venue-sport pairs, valid
rating and crowd-level values, and opening times before closing times. It
verified that startup populated all seven application tables with 100 rows each
in a disposable PostgreSQL database, and ran formatting, Checkstyle, and PMD
checks. This completed seeder coverage for all application tables while
preserving the existing client and venue seeder structure.

Token usage below is a cumulative snapshot for this conversation recorded on
November 18, 2026, at 15:29:45 America/New_York (20:29:45 UTC). It includes the
seeder work, follow-up discussion, and initial report preparation, but excludes
work after that snapshot. Cached input is included in input tokens, and
reasoning output is included in output tokens; neither is added again to the
total.

```text
input_tokens            : 4486320
cached_input_tokens     : 4145280
cache_write_input_tokens: 0
output_tokens           : 27133
reasoning_output_tokens : 8281

total_tokens            : 4513453
uncached_input_tokens   : 341040
```

Source: this conversation's recorded Codex token-count events. This is a
snapshot, not a final session total or a count of unique words in the context
window.
