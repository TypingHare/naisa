# Proposal

## Part 1

## Part 2

## Part 3

## Part 4

Below are the tools we are going to use in this project (excluding GitHub, which is mandatory):

1. Build tool & dependency manager: **Maven**. Some of our teammates have limited experience developing Java web applications. Although **Gradle** is more modern and used more frequently nowadays, it requires developers to learn how to write Gradle scripts (`build.gradle`) or Kotlin scripts (`build.gradle.kts`). Also, Maven is used widely in many legacy Java projects (many Java projects are kinda legacy) and still has a large market share.
2. Test runner: **JUnit 5**. This is the standard testing framework for a Spring Boot project.
3. Tools for testing API endpoints: **Postman** and/or **cURL**. **Postman** is a mature tool for testing and managing API endpoints, while **cURL** is a simple command-line tool for sending HTTP requests. Team members can choose whichever they prefer.
4. Mocking framework: **Mockito**. It is the primary and default mocking framework in a Spring project.
5. Test coverage tracker: **JaCoCo**. It is a powerful test coverage tool that shows which lines are not covered by unit tests.
6. Linters: **Checkstyle** and **PMD**. People now often refer to static code analysis tools as linters. A linter should normally identify potential bugs and check code style, but neither **Checkstyle** nor **PMD** can do both, and there are no other suitable alternatives. Therefore, we use both tools for linting.
7. Formatter: **google-java-format**. We want our code to comply with the Google Java Style Guide, and **google-java-format** can save us the time we would otherwise spend formatting files manually. It parses the code into an AST and formats it based on the rules defined in the Google Java Style Guide.
8. IDE/editor: **IntelliJ IDEA** / **Visual Studio Code** / **Neovim**. Team members can choose the IDE or editor that works best for them.
9. Debugging: **IntelliJ IDEA** / **GenAI**. IntelliJ IDEA comes with a powerful debugger that makes the tedious work of debugging easier. Sometimes GenAI can find bugs much faster than humans, but it may identify a superficial problem instead of the underlying one. We think combining a debugger with GenAI works best for us.
10. Continuous integration: **GitHub Actions**. It is a popular and useful tool that is worth learning.
11. Project management: **GitHub Projects**. Our team has only 4 members, so it is pretty small. Professional project management tools seem like overkill, and we may spend too much time learning and using them. Since we already use GitHub to host our remote repository, we will use GitHub Projects to manage this project.
