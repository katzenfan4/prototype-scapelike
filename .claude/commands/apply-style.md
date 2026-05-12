Review and fix style violations in this codebase against `styling_conventions.md`.

## Inputs

`$ARGUMENTS` may be:
- Empty — target all files changed vs the main branch (`git diff main...HEAD --name-only`) plus any unstaged changes (`git diff --name-only` and `git ls-files --others --exclude-standard`).
- One or more file paths — target only those files.

## Steps

1. Read `styling_conventions.md` in full.

2. Resolve the file list:
   - If `$ARGUMENTS` is non-empty, split on whitespace to get file paths.
   - Otherwise, run `git diff main...HEAD --name-only`, `git diff --name-only`, and `git ls-files --others --exclude-standard`. Union all results. Ignore deleted files.

3. Filter to source files only: `.java` files for backend rules, `.ts` / `.html` / `.css` files for frontend rules. Skip generated files (`dist/`, `target/`, `node_modules/`, `*.spec.ts`).

4. For each file, read it and check against every relevant rule in `styling_conventions.md`. Collect all violations before making any edits.

5. Fix every violation you can fix safely by editing the file directly:
   - Naming corrections (class names, file references in imports, variable names) — fix only within the file; flag cross-file renames separately.
   - Logger declaration format.
   - Exception wrapping pattern.
   - `private`/`public`/`readonly` access modifiers (TypeScript).
   - RxJS `$` suffix on Observables.
   - CSS class name casing.

6. After all edits, run the formatters for whichever file types were touched:
   - Any `.ts`/`.html`/`.css` changed → `npm run format --prefix client`
   - Any `.java` changed → `mvn -f server/pom.xml spotless:apply -q`

7. Do NOT fix:
   - Things that require renaming files on disk (report them instead).
   - Cross-file symbol renames (report with all affected locations).
   - Logic changes.

8. After all edits, print a summary grouped by file:

```
file/path.java
  FIXED   Logger field uses concatenation → parameterized
  FIXED   Import order: third-party before com.scapelike
  REPORT  Class named `PlayerSvc` → should be `PlayerService` (rename file too)

file/path.ts
  FIXED   Observable missing $ suffix: `messages` → `messages$`
  FIXED   Missing `private` on field `socket`
  REPORT  File named `GameComponent.ts` → should be `game.component.ts` (file rename required)
```

If no violations found, say so clearly.
