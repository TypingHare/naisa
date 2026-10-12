### Database

This document discusses how to set up and manage the database used in this
project.

#### Setup

We use [PostgreSQL][postgres] 18 as the only database in this project. Follow
the steps below to set it up:

1. Install PostgreSQL 18 and start the service. Verify the installation by
   running `psql --version`.
2. Create a database named `naisa` by running `createdb naisa`.
3. Copy `server/src/main/resources/database.example.properties` to
   `database.properties` in the same directory.
4. Set `spring.datasource.username` to your username, which you can obtain by
   running `psql -d naisa -Atc 'SELECT current_user;'`.

[postgres]: https://www.postgresql.org/about/
