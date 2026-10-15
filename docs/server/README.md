## Server

All documents in this directory discuss the `server` workspace, so all file
paths are relative to `server`. When developing the backend (server-side), we
open `server` as the project root directory in the IDE so that the IDE can find
`pom.xml` and recognize it as a Java project.

In [architecture.md](./architecture.md), we discuss the architecture of the
backend, which is a typical Spring Boot project. An application's architecture
refers to the major design decisions regarding dependencies (e.g., third-party
libraries and CLI tools), components (e.g., layers and utilities), communication
(e.g., protocols between endpoints), and so on.

In [venue-operations.md](./venue-operations.md), we describe the current venue
routes and how they differ from the initial proposal.
