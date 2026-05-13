Stage and commit all pending changes with short, logical commit messages. Never push.

## Commit message rules

- Format: `type(scope): subject` — Conventional Commits
- Subject: ≤ 50 chars, imperative mood, lowercase after colon
- Types: `feat`, `fix`, `refactor`, `chore`, `style`, `docs`, `test`
- Scope: short area name — `server`, `client`, `db`, `tick`, `ci`, `deps`, etc.
- No body unless the WHY is non-obvious from the subject alone
- No `Co-Authored-By` or any footer watermarks

## Steps

1. Run `git status` and `git diff HEAD` to understand all pending changes (staged and unstaged).

2. Group the changes into logical commits. Split when changes span genuinely separate concerns (e.g. a deps bump is separate from a feature; a formatter run is separate from a bug fix). Combine when changes are tightly coupled and only make sense together.

3. For each logical group:
   a. Stage only the files for that group using `git add <files>`.
   b. Write a commit message following the rules above.
   c. Commit with `git commit -m "<message>"`.

4. After all commits, run `git log --oneline -5` and show the output.

5. Do NOT run `git push` under any circumstances.
