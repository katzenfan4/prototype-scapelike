---
model: claude-haiku-4-5
---
Stage and commit all pending changes. Never push.

## Message format

`type(scope): subject` — Conventional Commits
- Subject: ≤50 chars, imperative mood, lowercase after colon
- Types: `feat` `fix` `refactor` `chore` `style` `docs` `test`
- Scopes: `server` `client` `db` `tick` `ci` `deps`
- No body unless WHY is non-obvious. No `Co-Authored-By` or footers.

## Steps

1. `git status` + `git diff HEAD` — understand all pending changes.
2. Group into logical commits. Split separate concerns (e.g. deps bump ≠ feature). Combine tightly coupled changes.
3. Per group: `git add <files>` → commit with `git commit -m "<message>"`.
4. `git log --oneline -5` — show result.
