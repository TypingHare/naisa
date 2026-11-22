MVN = ./mvnw

.PHONY: all format lint run-unit-tests run-integration-tests test \
	unit-coverage coverage start stop run-test download-sources clean

# Alias for `run-test`.
all: run-test

# Format all Java files using Spotless.
# See: https://github.com/diffplug/spotless
format:
	$(MVN) spotless:apply

# Lint all Java files.
lint:
	$(MVN) checkstyle:check
	$(MVN) pmd:check -Dpmd.printFailingErrors=true

# Run unit tests, excluding suites tagged with `@Tag("integration")`.
run-unit-tests:
	$(MVN) -DexcludedGroups=integration test

# Run integration tests.
run-integration-tests:
	$(MVN) -Dgroups=integration -DforkCount=1 -DreuseForks=true test

# Run both unit tests and integration tests.
test:
	$(MAKE) run-unit-tests
	$(MAKE) run-integration-tests

# Run unit tests and generate a JaCoCo coverage report.
# See: https://github.com/jacoco/jacoco
unit-coverage:
	$(MVN) -DexcludedGroups=integration clean jacoco:prepare-agent test \
		jacoco:report

# Run all tests and generate a JaCoCo coverage report.
# See: https://github.com/jacoco/jacoco
coverage:
	$(MVN) clean jacoco:prepare-agent test jacoco:report

# Start the service in the production environment.
start:
	$(MVN) spring-boot:start

# Stop the service in the production environment.
stop:
	$(MVN) spring-boot:stop

# Run the service in the test environment. This drops and recreates the test
# database. The database URL is defined in
# `src/main/resources/database-test.properties`. The password should be placed
# in `~/.pgpass`.
run-test:
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=test

# Download source files.
download-sources:
	$(MVN) dependency:sources

# Clean build artifacts.
clean:
	rm -rf target
