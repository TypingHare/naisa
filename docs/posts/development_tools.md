## Development Tools

<small>By **James Chen** Last modified on Sep 29, 2026</small>

Development tools play an important role in delivering high-quality, bug-free code. In this post, we will cover common development tools used in Java projects. Java frameworks are covered separately in `docs/posts/java_web_project.md`.

If you have very limited experience developing backend services, the number of tools may seem intimidating. However, you don't need to learn them all thoroughly before getting started. Instead, you should learn them as you go through the development cycle. Don't be afraid of making mistakes.

### IDE (Integrated Development Environment)

An **IDE** is an application that helps you edit, examine, and ship code more efficiently.

As of 2026, the most popular IDEs for Java projects are IntelliJ IDEA by JetBrains and Visual Studio Code by Microsoft. Obselete editors and IDEs include Eclipse and Apache NetBeans. I personally use Neovim, a niche terminal-based editor.

I highly recommend IntelliJ IDEA for debugging. Its built-in debugger is the GOAT. I do most of my development and testing in Neovim and the terminal, but when I need to debug a program, I often switch to IntelliJ IDEA.

### Package Manager & Build Tool - Maven

#### Maven as a Package Manager

A **package manager** is a useful tool that resolves and downloads dependencies for you. For example, `apt` is the built-in package manager on Debian. It allows you to install an application with a single command. For instance,

```bash
apt install -y default-jdk
```

Java is then downloaded from the repository to your machine and installed automatically. Once the command completes, Java will be available in your environment!

Common build tools for Java projects include Maven and Gradle, both of which also manage dependencies. In this project, we use Maven, as it is more traditional and is still widely used in legacy Java projects.

The `server/pom.xml` file is the configuration file for the `server` workspace. It defines metadata for the project, such as the parent artifact, Java version, plugins, and dependencies. Let's focus on these lines:

```xml
<dependencies>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc</artifactId>
  <version>4.1.1</version>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <version>4.1.1</version>
  <scope>test</scope>
</dependency>
</dependencies>
```

This section defines two dependencies: `spring-boot-starter-webmvc` and `spring-boot-starter-webmvc-test`. These starters provide dependencies for developing and testing Spring Boot web applications. When we run the following command, the dependencies specified in `pom.xml` will be resolved, and their compiled artifacts (e.g., JAR files) will be downloaded from a Maven repository:

```bash
mvn dependency:resolve
```

#### Maven Wrapper

Developers may have different versions of Maven in their environments, which can cause unexpected problems and inconsistencies across the project. Therefore, instead of using the Maven installed in each developer's environment, we use the Maven Wrapper.

The `server/mvnw` (for Unix-like operating systems such as Linux and macOS) and `server/mvnw.cmd` (for Windows) files are executable Maven Wrapper scripts. The wrapper's configuration files are stored in `server/.mvn`. Now, let's try running this command in the `server` workspace:

```bash
./mvnw dependency:resolve
```

The files are usually stored at `~/.m2/repository/` in a Unix-like operating system.

#### Maven as a Build Tool

Maven is primarily a build tool. The following command compile, test, and create a JAR file:

```bash
./mvnw package
```

To only compile and run tests, execute:

```bash
./mvnw
```

### Linter - PMD

A **static code analysis tool**, often called a **linter**, analyzes code without running it. PMD is a classic linter for Java.

```bash
./mvnw pmd:check -Dpmd.printFailingErrors=true
```

Linters often report style issues as well, but PMD has limited support for checking formatting. Therefore, we use `maven-checkstyle-plugin` to check code style:

```bash
./mvnw checkstyle:check
```

### Code Formatter - google-java-format

A code formatter formats code according to preset rules, which are usually defined in a configuration file in the project's root directory. It is important for developers working on the same project to follow the same style guide.

In this project, we use [google-java-format](https://github.com/google/google-java-format). It does not support customization, as its goal is to ensure that code formatting complies with the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). In this project, Checkstyle is configured to match the formatting rules used by google-java-format. Therefore, after you format a Java file with google-java-format, Checkstyle should not report any style violations.

Please make sure you install the plugin or extension with the same name in your IDE and enable "format on save". This allows your IDE to format files automatically when you save them.

### Debugger

Everyone makes mistakes when writing code, resulting in bugs. Some bugs are hard to find, and therefore we need some tools to collect clues. Choose a good debugger plugin for Java projects on your IDE. If you use IntelliJ IDEA, then the debugger already comes with it.

But in the AI era, if you have ample tokens, I recommend using AI to help you debug. Please keep in mind that AI may not find the root cause. In my experience, it sometimes patches the code just to pass the tests instead of fixing the underlying problem. This is because fixing some bugs requires rewriting functions, and AI tends to avoid changing existing code unless explicitly prompted.

### Code Coverage - JaCoCo

An application is not reliable until its code coverage reaches 100%. However, 100% code coverage does not necessarily mean the application is bug-free. Regardless, we need a tool to track code coverage. JaCoCo is a Java code coverage tool. Run the following command to generate a code coverage report at `server/target/site/jacoco/jacoco.xml`.

```bash
mvn clean jacoco:prepare-agent test jacoco:report
```

The report includes four coverage metrics: instruction, branch, line, and method coverage. It also shows exactly which lines of source code are not covered.

A pull request should not be approved and merged unless instruction coverage is 100%.

### Postman and cURL

Postman is a popular tool for building, testing, sharing, and managing APIs. It is powerful, but in our project, we simply use it as an option for testing the HTTP APIs our service provides. Postman's user interface is pretty straightforward. If you find it confusing, read `docs/posts/http.md` and `docs/posts/java_project.md` again. What you really need is to understand what the service provides, rather than how to use Postman.

While Postman is a GUI tool, cURL is a CLI tool that does the same job if we only consider sending HTTP requests and analyzing responses. One advantage of cURL is that you can modify HTTP requests simply by editing files, which saves you from switching between the keyboard and mouse. You can learn cURL by reading the help information by running `curl --help`.
