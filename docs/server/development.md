### Development

This document specifies a set of rules of developing the Spring Boot application
and introduces development-related tools and commands.

#### Javadoc

Please add an `@author` tag to each class or method you add or modify. For
classes in `com.naisa.controller`, `com.naisa.service`, and
`com.naisa.repository`, add the tag to each method you add or modify. For all
other classes in the codebase, add the tag to the class you add or modify.
Separate names in the tag with commas, for example:

```java
/**
 * Description of a class or method.
 *
 * @author James Chen, Andrew Hsu, Vincent Lai, Emily Wang
 */
```

If generative AI tools or agents are used to generate all or part of a method or
class, authors must include their full names along with the names of the tools
or agents they used, separated by commas:

```java
/**
 * Description of a class or method.
 *
 * @author James Chen, Codex
 */
```

The name of each tool or agent must appear immediately after the name of the
author who used it. If two developers use the same AI agent (e.g., Codex), then
the agent's name can appear twice in the `@author` tag. For example:

```java
/**
 * Description of a class or method.
 *
 * @author James Chen, Codex, Vincent Lai, Claude Code, Andrew Hsu, Codex
 */
```

#### Makefile Recipes

The `Makefile` provides several useful targets (commands). Run them with
`make <target-name>` in the `server` workspace. Please ensure that GNU Make 3.8
or later is installed in your environment.

1. `make format`: Format all Java files using Spotless.
2. `lint`: Run Checkstyle and PMD for linting. Run this command before making a
   commit.
3. `test`: Run all tests.
4. `coverage`: Run JaCoCo and generate a coverage report.
5. `run`: Start the service.
6. `run-test`: Start the service with the "test" profile active.
7. `download-sources`: Download source files for Java dependencies.
