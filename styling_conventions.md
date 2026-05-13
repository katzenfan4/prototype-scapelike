# Styling Conventions

Authoritative style reference for both implementation and code review. Covers the Java 21/Javalin 6 backend and the Angular 20/TypeScript frontend.

---

## Naming

- Names must be descriptive. No single-letter variables (except loop indices `i`, `j`, `k`), no opaque abbreviations like `tmp`, `val`, `data`, `flag`, `info`, `x`, `obj`.
- Idiomatic short forms are fine where universally understood: `ctx` (context), `db` (database), `conn` (connection), `stmt` (statement), `msg` (message).
- When in doubt, spell it out — `playerSession` beats `ps`, `tickInterval` beats `ti`.

## Comments and documentation

- Add a comment only when the **why** is non-obvious: a hidden constraint, a workaround, a subtle invariant, or non-intuitive behavior. Do not describe what the code already says.
- Public methods and classes get a brief doc comment when their purpose or contract is not immediately clear from the name alone.
- Complex methods or non-trivial algorithms get a comment block explaining the approach, key assumptions, and any gotchas. Simple delegation or CRUD methods do not.
- `// TODO:` comments are fine for unimplemented stubs — include what needs to be done, not just the word TODO.

```java
// Tick budget is 600 ms — if processing exceeds this, the next tick is delayed
private void tick() { ... }
```

```typescript
// Server echoes messages back; real routing is not yet implemented
private handleMessage(raw: string): void { ... }
```

---

## Backend — Java 21 / Javalin 6

### Packages

- Root: `com.scapelike`
- Sub-packages: lowercase, single-word, domain-grouped

| Package | Purpose |
|---|---|
| `com.scapelike.db` | Database connection and migrations |
| `com.scapelike.tick` | Game tick loop |
| `com.scapelike.server` | HTTP / WebSocket entry point |
| `com.scapelike.game` | (planned) Game state and logic |
| `com.scapelike.world` | (planned) World / map model |

### Class naming

- PascalCase, semantic names, one public class per file.

| Suffix | When to use | Example |
|---|---|---|
| `Engine` | Tick/game-loop processor | `TickEngine`, `CombatEngine` |
| `Server` | HTTP/WS entry point | `GameServer` |
| `Handler` | Extracted route or message handler | `PlayerHandler` |
| *(none)* | Resource wrapper / manager | `Database` |

### Fields and variables

- camelCase for fields, locals, and parameters.
- Follow the shared [Naming](#naming) rules above — descriptive names, idiomatic short forms only.
- Constants: `UPPER_SNAKE_CASE` declared as `private static final` (or `public static final` if shared across classes).

```java
private static final long TICK_MS = 600;
private final Map<String, WsContext> sessions = new ConcurrentHashMap<>();
```

### Logging

```java
private static final Logger log = LoggerFactory.getLogger(ClassName.class);
```

- Parameterized messages only — no string concatenation.

```java
log.info("Client connected: {}", ctx.getSessionId());   // correct
log.info("Client connected: " + ctx.getSessionId());    // wrong
```

| Level | Use for |
|---|---|
| `info` | Lifecycle events (startup, connect, disconnect) |
| `debug` | Per-tick / per-message noise |
| `warn` | Recoverable unexpected states |
| `error` | Failures that affect correctness |

### Exception handling

- Use try-with-resources for every `AutoCloseable` (`Connection`, `Statement`, streams).
- Wrap checked exceptions into `RuntimeException` with a descriptive message and the original cause.
- Never swallow exceptions silently.

```java
try (Connection conn = pool.getConnection(); Statement stmt = conn.createStatement()) {
    stmt.execute(sql);
} catch (SQLException e) {
    throw new RuntimeException("Migration failed", e);
}
```

### Environment variables

- Keys: `UPPER_SNAKE_CASE` (e.g., `DB_HOST`, `TICK_MS`).
- Access via a static `env(String key, String fallback)` helper — always provide a local-dev fallback.
- Read at construction/startup time, never inline during request handling.

```java
private static String env(String key, String fallback) {
    String val = System.getenv(key);
    return val != null ? val : fallback;
}
```

### Dependency injection

Constructor injection only. No field injection.

```java
public GameServer(TickEngine tickEngine, Database db) { ... }
```

### Concurrency

| Situation | Tool |
|---|---|
| Shared map | `ConcurrentHashMap` |
| Shared counter | `AtomicLong` / `AtomicInteger` |
| Timed task | `ScheduledExecutorService` via `Executors.newSingleThreadScheduledExecutor()` |

### Imports

- Explicit only — no wildcards.
- Single alphabetical block — no blank lines between groups (enforced by Palantir Java Format via Spotless).

### Modern Java 21 style

- Text blocks for multi-line strings (SQL, JSON payloads).
- Lambdas and method references over anonymous classes.
- No Lombok — keep the dependency list minimal.

---

## Frontend — Angular 20 / TypeScript strict

### File naming

All files: kebab-case.

| Type | Convention | Example |
|---|---|---|
| Component | `feature-name.ts` + `.html` + `.css` | `game-hud.ts` |
| Service | `feature-name.service.ts` | `game.service.ts` |
| Config | `feature-name.config.ts` | `app.config.ts` |
| Guard | `feature-name.guard.ts` | `auth.guard.ts` |
| Pipe | `feature-name.pipe.ts` | `tick.pipe.ts` |

### Class naming

- Components: PascalCase, **no** `Component` suffix — `App`, `GameHud`, `WorldMap`.
- Services: PascalCase + `Service` — `GameService`, `WorldService`.
- Config exports: `const appConfig` or `class AppConfig`.

### Angular decorators

- Standalone components only — always include `imports` array, never use NgModule `declarations`.
- `@Injectable({ providedIn: 'root' })` for all singleton services.
- Use `templateUrl` + `styleUrl` (external files), not inline `template`/`styles`.

```typescript
@Component({
  selector: 'app-root',
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App { ... }
```

### TypeScript

- Full strict mode is enforced (`strict: true`, `strictTemplates: true`). No `any`.
- Definite assignment assertion (`!`) only on properties initialized in lifecycle hooks.
- All class members need explicit `private` or `public` — default to `private`.
- `readonly` on properties assigned once (injected services, public streams).

```typescript
constructor(public readonly game: GameService) {}  // injected, template-accessed
private sub!: Subscription;                         // initialized in ngOnInit
```

### RxJS

- `$` suffix on every Observable/Subject: `messages$`, `state$`.
- `readonly` for public streams.
- Manual subscription lifecycle — store, then unsubscribe in `ngOnDestroy`.
- Prefer `Subject` / `BehaviorSubject` directly; avoid unnecessary pipe wrappers when not needed.

```typescript
readonly messages$ = new Subject<string>();

private sub!: Subscription;

ngOnInit(): void {
  this.sub = this.game.messages$.subscribe(msg => this.messages.push(msg));
}

ngOnDestroy(): void {
  this.sub.unsubscribe();
}
```

### Constants

- File-level constants: `const wsUrl = '...'` — camelCase, placed before the class declaration.
- Class-level immutables: `readonly` property — not a separate constant.

### Templates

```html
[prop]="expr"           <!-- property binding -->
(event)="handler()"     <!-- event binding -->
*ngIf="condition"       <!-- structural directive -->
*ngFor="let x of xs"    <!-- structural directive -->
{{ expr }}              <!-- interpolation -->
[class.active]="bool"   <!-- conditional class -->
```

- Ternary expressions are OK for simple conditions inside interpolation.
- Avoid complex logic in templates — move it to the component class.

### CSS

- One stylesheet per component, colocated with the component file.
- Class names: kebab-case, flat structure — no BEM, no nesting conventions.
- Element selectors are OK for scoped base styles within a component.

### Formatting

Enforced by Prettier + EditorConfig — do not override manually.

| Rule | Value |
|---|---|
| Line width | 100 characters |
| Quotes (TS/JS) | Single |
| Indentation | 2 spaces |
| HTML parser | Angular |
| Final newline | Required |
| Trailing whitespace | Trimmed |
