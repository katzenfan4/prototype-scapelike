Rebuild and redeploy the full project locally using Docker Compose.

## Arguments

`$ARGUMENTS` — pass `test` or `--test` to run server tests during the Maven build. Default: tests skipped.

## Steps

1. Determine test mode: if `$ARGUMENTS` contains `test`, use `SKIP_TESTS=false`; otherwise `SKIP_TESTS=true`.

2. Run formatters on all source files before building:
   - `npm run format --prefix client`
   - `mvn -f server/pom.xml spotless:apply -q`

3. Run `docker-compose down` to stop and remove existing containers.

4. Run `SKIP_TESTS=<value> docker-compose up --build -d` to rebuild all images and start containers detached.

5. Run `docker-compose ps` and show the output.

6. If any service is not running, run `docker-compose logs --tail=40 <service>` for that service and report the output so the user can diagnose the failure.
