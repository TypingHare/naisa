## Java Web Project

<small>By **James Chen** Last modified on Sep 30, 2026</small>

### Basic Directory Structure of a Java Project

A Java web project usually has a bloated directory structure. The `server` workspace presents a typical Java web project directory structure:

```bash
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── naisa
│   │   │           └── Application.java
│   │   └── resources
│   │       ├── static
│   │       ├── templates
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── naisa
│                   └── ApplicationTest.java
├── mvnw
├── mvnw.cmd
└── pom.xml
```

The `mvnw`, `mvnw.cmd`, and `pom.xml` files have been discussed in `docs/posts/development_tools.md`. The `src` directory stores all source code and resources. The `main` directory stores implementation code and resources, while the `test` directory stores test code and resources.

There are two directories in `server/src/main`: `java` and `resources`. It is easy to infer what they store, but you may wonder, as I did many moons ago, why we need the `java` directory. Why don't we make the directory structure flatter? It turns out that in a larger project, developers may use other programming languages, such as Python and JavaScript, to support some of the project's functionality. So, we can create a `python` directory alongside the `java` directory to keep the project more organized.

The root package name for this project is `com.naisa`, so its Java source files are organized under `com/naisa` (from `server/src/main/java`) within the `java` directory.

The root directory for Java test files is `server/src/test/java/com/naisa`. The paths of test files should mirror those of the corresponding implementation files in the `main` directory. For example, the following two files form a pair:

- `server/src/main/java/com/naisa/Application.java`
- `server/src/test/java/com/naisa/ApplicationTest.java`

### Spring 7

Because Java runs on a virtual machine (i.e., the JVM), it is pretty flexible. Built on Java's annotation and reflection features, the Spring framework supports inversion of control (IoC).

Let's first take a look at this class:

```java
class OrderService {
    private final PaymentService paymentService = new PaymentService();
}
```

Here, `OrderService` depends on `PaymentService` because creating an `OrderService` instance also requires creating a `PaymentService` instance. In the example above, `OrderService` creates its own dependency. It would be more flexible if it received the dependency through its constructor:

```java
class OrderService {
    private final PaymentService paymentService;

    OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

In this case, the `PaymentService` instance is created outside `OrderService` instead of within it. As a project grows larger, creating instances and injecting them into other instances becomes tedious, and we want to automate the process. To support this, the Spring framework provides the `@Component` annotation, which can be added above a class:

```java
@Component
class PaymentService {
}

@Component
class OrderService {
    private final PaymentService paymentService;

    OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

During initialization, Spring recursively scans a specified package and collects all classes annotated with `@Component`. It then resolves their dependencies, creates instances of these classes, and stores them in its IoC container. By default, these instances are singletons, meaning that each bean definition has only one corresponding object in the container. You can access an instance (called a Spring bean) like this:

```java
final ApplicationContext context = SpringApplication.run(Application.class, args);
final OrderService orders = context.getBean(OrderService.class);
```

### Servlet and Tomcat

As stated in `docs/posts/http.md`, a web backend service has to handle an incoming HTTP request, which is just a string, and send an HTTP response back to the client. This process is encapsulated in a servlet. Below is an example of a servlet that handles a `GET /hello` request:

```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        final String name = request.getParameter("name");
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("Hello, " + name + "!");
    }
}
```

But defining servlets does not automatically enable a Java program to handle incoming requests. Apache Tomcat is a web server and servlet container. It accepts HTTP requests, routes them to the appropriate servlet, and manages servlet lifecycles.

Spring Boot, which will be discussed soon, uses Tomcat to start the service. Therefore, you can find log entries related to Tomcat when you run `./mvnw spring-boot:run` in the `server` workspace.

### Spring Boot 4

Spring Boot provides many annotations and features for web projects. The `server/src/main/java/com/naisa/Application.java` file serves as the entry point for a Spring Boot project:

```java
@SpringBootApplication
public class Application {
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

The philosophy behind Spring Boot and other frameworks in the Spring family, in my opinion, is to abstract away repetitive implementation details for web services, allowing developers to focus more on business logic. However, beginners often have no idea what happens under the hood and struggle during their first few weeks using Spring Boot.

I cannot explain everything the framework does behind `SpringApplication.run(Application.class, args);`. However, it is clear that it recursively scans the package containing the `Application` class and its subpackages for Spring components and initializes Spring's IoC container. Controller methods (i.e., methods annotated with `GetMapping` or other annotations ending with `Mapping` in classes annotated with `RestController`) are then registered as request handlers. Spring's `DispatcherServlet`, which routes requests to these handlers, is created and registered with Tomcat.

We will learn more about the all sorts of frameworks in the Spring family in this project.
