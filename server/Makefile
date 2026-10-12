MVN = ./mvnw

.PHONY: all lint test coverage run

all:
	$(MVN) package

lint:
	$(MVN) checkstyle:check
	$(MVN) pmd:check -Dpmd.printFailingErrors=true

test:
	$(MVN) test

coverage:
	$(MVN) clean jacoco:prepare-agent test jacoco:report

run:
	$(MVN) spring-boot:run
