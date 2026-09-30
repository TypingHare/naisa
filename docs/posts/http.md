## HTTP (Hypertext Transfer Protocol)

<small>By **James Chen** Last modified on Sep 29, 2026</small>

The Hypertext Transfer Protocol (HTTP) is one of the most important application-layer protocols in web application development. It defines rules that clients and servers follow so they can communicate smoothly. In this post, we will be covering some basic knowledge about this protocol.

First, we need to define a few terms. In web development, servers and clients are applications, not end systems (i.e., devices such as phones and laptops). A server handles incoming requests and sends responses to clients.

But how does this work under the hood? Every end system on the Internet has an IP address. An end system can send a message to another by specifying the destination’s IP address. However, a device can run multiple applications, so when it receives a message, it must determine which application the message is intended for.

On modern operating systems, sockets provide a way to do this. A daemon (or service) is a running process that can create a listening socket and bind it to a port to accept connections. For example, if a daemon runs on a computer with the IP address `112.47.9.220` and listens on port `471`, then `112.47.9.220:471` identifies where to send messages intended for that daemon.

There are two things we need to keep in mind. First, `localhost` is a **hostname** that normally resolves to `127.0.0.1`, a special IP address called a **loopback address**. Messages sent to this address are delivered to the sender’s own device. Second, the default port for HTTP is 80, and the default port for HTTPS is 443. When a browser opens `http://localhost/path`, it connects to `127.0.0.1:80`. When developing a backend application, we often need to run the server locally. Because binding to port 80 usually requires elevated privileges, we typically have it listen on another port, such as `8080`.

Two daemons generally cannot listen on the same port on the same machine at the same time. Therefore, trying to bind to port that are already in use will cause an error.

### HTTP As an Application Layer Protocol

The application layer is the top layer of the network stack, and it is usually easy to understand. For example, if a user needs to send a message, an application-layer protocol defines how that message should be formatted and interpreted.

We need to learn a command-line tool: `nc`, which is widely available on Unix-like systems (e.g., Linux and macOS). It can listen on a port and print the data it receives:

```bash
nc -l localhost 8080
```

Upon running this command, `nc` listens on port `8080` for an incoming connection.

Open a new terminal session and run the following command:

```bash
curl -X POST http://localhost:8080/ --data "Hello world\!"
```

When we switch back to the terminal session running `nc`, we should see something like this:

```text
POST / HTTP/1.1
Host: localhost:8080
User-Agent: curl/8.7.1
Accept: */*
Content-Length: 12
Content-Type: application/x-www-form-urlencoded

Hello world!
```

This is what an HTTP request looks like. The tool we used to send it is called cURL. In the `curl` command, `-X POST` sets the **HTTP request method** to POST, and `--data 'Hello world!'` sets the **request body**.

The first line of an HTTP request has three components separated by spaces: the HTTP request method, the path, and the protocol version. The lines that follow, up to the first empty line, are the **request headers**. They serves as the metadata of the request. Each header contains a key and a value, separated by a colon. The section after the first empty line is the **request body**.

An HTTP server can handle a request like this. It usually parses the request and creates an object for downstream components (e.g., handlers) to process. Below is an example of the simplest Java class to represent an HTTP request.

```java
public record HttpRequest(
    String method,
    String path,
    String protocol,
    Map<String, String> headers,
    String body
) {}
```

An HTTP response is in a similar form:

```text
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8
Content-Length: 13

Hello, world!
```

The first line consists of the protocol version and an [**HTTP response status code**](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes) (or simply **status code**). It is followed by headers, an empty line, and the **response body**. Common status codes include:

- 200 OK
- 201 Created
- 204 No Content
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 500 Internal Server Error
- 502 Bad Gateway

### URL (Uniform Resource Locator)

A URL consists of a protocol, a domain, a path, and an optional query string. For example, consider the following URL:

```text
https://www.google.com/search?client=safari&rls=en&q=java&oe=UTF-8
```

The protocol is `https` (the string before `://`), the domain is `www.google.com`, the path is `/search` (the string between the domain and the question mark), and the query string is `client=safari&rls=en&q=java&oe=UTF-8` (the string after the question mark).

The query string is a collection of key-value pairs separated by `&`, with `=` separating each key from its value. The query string above can therefore be represented as follows:

```text
client = safari
rls = en
q = java
oe = UTF-8
```

### HTTPS

HTTP sends requests and responses in plain text, which can be intercepted by hackers. If you go to a friend's home, connect to their Wi-Fi, and access a website using HTTP, your requests first pass through the router, which is under your friend's full control. If your friend knows enough about computer networks, they can see the HTTP requests and responses, including the pages you visit.

For security, we want connections between clients and servers to be encrypted, which is why HTTPS was introduced. HTTPS establishes a secure connection between a client and a server. During the connection setup, the client and server securely establish shared encryption keys. They then use these keys to encrypt and decrypt the messages they exchange.

### Troubleshoot Port Already In Use

I can still recall how frustrated I was when I saw "Port 8080 already in use" on the screen when I first started learning Spring Boot. If you run into the same issue, see if the following commands can help you out. If not, don't hesitate to ask AI.

```bash
# on Linux
ss -ltnp 'sport = :8080'

# on macOS
lsof -i:8080
```

You should see something like this on macOS:

```text
COMMAND  PID  USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
nc      4724 james    5u  IPv4 0x767c11c0174118b5      0t0  TCP localhost:http-alt (LISTEN)
```

You don't need to understand every column to see that the process with PID `4724` is listening on port `8080`. Let's forcibly terminate it using the following command:

```bash
kill -9 4724
```

The process will be terminated, and the port will become available again.
