MVN = ./mvnw

.PHONY: all lint test unit coverage unit-coverage run run-test download-sources

all:
	$(MVN) package

lint:
	$(MVN) checkstyle:check
	$(MVN) pmd:check -Dpmd.printFailingErrors=true

test:
	$(MVN) test

unit:
	$(MVN) '-Dtest=*Test,!ApplicationTest' test

coverage:
	$(MVN) clean jacoco:prepare-agent test jacoco:report

unit-coverage:
	$(MVN) '-Dtest=*Test,!ApplicationTest' clean jacoco:prepare-agent test jacoco:report

run:
	$(MVN) spring-boot:run

run-test:
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=test

download-sources:
	$(MVN) dependency:sources
