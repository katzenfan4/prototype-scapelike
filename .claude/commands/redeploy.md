Rebuild and redeploy the project locally using Docker Compose. Supports full-stack mode (mariadb + server + client) and dev mode (mariadb + server only, for use with a local `ng serve`).

## Arguments

`$ARGUMENTS` may contain any combination of:
- `test` or `--test` — run server tests during the Maven build. Default: tests skipped.
- `dev` or `--dev` — use `docker-compose.dev.yml` (backend-only; no client container). Default: `docker-compose.yml` (full stack).

## Steps

1. Determine mode from `$ARGUMENTS`:
   - If `$ARGUMENTS` contains `dev`, set compose file to `docker-compose.dev.yml`; otherwise `docker-compose.yml`.
   - If `$ARGUMENTS` contains `test`, use `SKIP_TESTS=false`; otherwise `SKIP_TESTS=true`.

2. Run formatters on all source files before building:
   - `npm run format --prefix client`
   - `mvn -f server/pom.xml spotless:apply -q`

3. Run `docker-compose -f <compose-file> down` to stop and remove existing containers.

4. Run `SKIP_TESTS=<value> docker-compose -f <compose-file> up --build -d` to rebuild images and start containers detached.

5. Run `docker-compose -f <compose-file> ps` and show the output.

6. If any service is not running, run `docker-compose -f <compose-file> logs --tail=40 <service>` for that service and report the output so the user can diagnose the failure.

7. If dev mode is active, remind the user that the client container is not running and they should start it separately: `cd client && ng serve`.
