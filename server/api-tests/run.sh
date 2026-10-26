#!/usr/bin/env bash
# Run the complete HTTP suite against an isolated database and collect branch coverage.
# Author: Andrew Hsu, Codex
set -euo pipefail
cd "$(dirname "$0")/.."
server_dir="$PWD"
run_name="naisa-api-$$"
db_name="${run_name}-db"
app_name="${run_name}-app"
network_name="${run_name}-network"
port="${API_TEST_PORT:-18080}"
mkdir -p target/api-tests

cleanup() {
  docker logs "$app_name" > target/api-tests/server.log 2>&1 || true
  docker rm -f -v "$app_name" "$db_name" >/dev/null 2>&1 || true
  docker network rm "$network_name" >/dev/null 2>&1 || true
}
trap cleanup EXIT

./mvnw --batch-mode -DskipTests package dependency:copy \
  -Dartifact=org.jacoco:org.jacoco.agent:0.8.14:jar:runtime \
  -DoutputDirectory=target/api-tests-tools
rm -f target/api-tests.exec
docker network create "$network_name" >/dev/null
docker run -d --name "$db_name" --network "$network_name" --network-alias db \
  -e POSTGRES_DB=naisa_api_tests -e POSTGRES_USER=naisa -e POSTGRES_PASSWORD=api_test_only \
  postgres:17-alpine >/dev/null
for attempt in {1..60}; do
  if docker exec "$db_name" pg_isready -U naisa -d naisa_api_tests >/dev/null 2>&1; then break; fi
  sleep 1
done
docker exec "$db_name" pg_isready -U naisa -d naisa_api_tests

docker run -d --name "$app_name" --network "$network_name" \
  -p "127.0.0.1:${port}:8080" -v "$server_dir/target:/app" \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/naisa_api_tests \
  -e SPRING_DATASOURCE_USERNAME=naisa -e SPRING_DATASOURCE_PASSWORD=api_test_only \
  eclipse-temurin:21-jdk java \
  -javaagent:/app/api-tests-tools/org.jacoco.agent-0.8.14-runtime.jar=destfile=/app/api-tests.exec \
  -jar /app/demo-2026.1.jar \
  --spring.config.import=optional:classpath:api-tests-local.properties \
  --spring.jpa.hibernate.ddl-auto=validate --spring.jpa.open-in-view=false >/dev/null
for attempt in {1..90}; do
  if curl --fail --silent "http://127.0.0.1:${port}/v1/health" >/dev/null; then break; fi
  if [ "$(docker inspect -f '{{.State.Running}}' "$app_name")" != true ]; then
    docker logs "$app_name"
    exit 1
  fi
  sleep 1
done
curl --fail --silent "http://127.0.0.1:${port}/v1/health" >/dev/null
docker exec -i "$db_name" psql -v ON_ERROR_STOP=1 -U naisa -d naisa_api_tests < api-tests/seed.sql

# Keep coverage and test results even when Newman finds failures.
api_status=0
npx --yes newman@6.2.3 run api-tests/naisa-venue-ops.postman_collection.json \
  --env-var "baseUrl=http://127.0.0.1:${port}" --delay-request 20 \
  --reporters cli,junit --reporter-junit-export target/api-tests/results.xml || api_status=$?
if [ "$api_status" -eq 0 ]; then
  docker exec -i "$db_name" psql -v ON_ERROR_STOP=1 -U naisa -d naisa_api_tests \
    < api-tests/assertions.sql || api_status=$?
fi
docker stop --time 20 "$app_name" >/dev/null
./mvnw --batch-mode jacoco:report -Djacoco.dataFile=target/api-tests.exec
if [ "$api_status" -ne 0 ]; then exit "$api_status"; fi

python3 - <<'PY'
from pathlib import Path
import shutil
import xml.etree.ElementTree as ET

report = Path('target/site/jacoco')
root = ET.parse(report / 'jacoco.xml').getroot()
branch = next(c for c in root.findall('counter') if c.get('type') == 'BRANCH')
covered, missed = int(branch.get('covered')), int(branch.get('missed'))
percentage = covered * 100 / (covered + missed)
print(f'API branch coverage: {covered}/{covered + missed} ({percentage:.1f}%)')
if percentage < 55:
    raise SystemExit('API branch coverage is below the 55% submission requirement.')
destination = Path('../docs/server/coverage/api-tests')
if destination.exists():
    shutil.rmtree(destination)
shutil.copytree(report, destination, ignore=shutil.ignore_patterns('jacoco.csv'))
shutil.copyfile('target/api-tests/results.xml', destination / 'api-results.xml')
print(f'Saved HTML coverage report: {destination / "index.html"}')
PY
