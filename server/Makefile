MVN = ./mvnw

.PHONY: all lint test coverage run

all:
	$(MVN) package

lint:
	$(MVN) pmd:check -Dpmd.printFailingErrors=true
	$(MVN) checkstyle:check

test:
	$(MVN) test

coverage:
	$(MVN) clean jacoco:prepare-agent test jacoco:report

run:
	$(MVN) spring-boot:run
