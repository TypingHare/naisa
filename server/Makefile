MVN = ./mvnw

.PHONY: all lint test coverage api-test run run-test download-sources

all:
	$(MVN) package

lint:
	$(MVN) checkstyle:check
	$(MVN) pmd:check -Dpmd.printFailingErrors=true

test:
	$(MVN) -Dspring.profiles.active=test test

coverage:
	$(MVN) clean jacoco:prepare-agent test jacoco:report

api-test:
	bash api-tests/run.sh

run:
	$(MVN) spring-boot:run

run-test:
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=test

download-sources:
	$(MVN) dependency:sources

# Keep database-free targets separate from the API runner added by PR #39.
UNIT_TESTS = *Test,!ApplicationTest,!NumericRequestTest,!VenueSportPersistenceTest

.PHONY: unit unit-coverage

unit:
	$(MVN) '-Dtest=$(UNIT_TESTS)' test

unit-coverage:
	$(MVN) '-Dtest=$(UNIT_TESTS)' clean jacoco:prepare-agent test jacoco:report
