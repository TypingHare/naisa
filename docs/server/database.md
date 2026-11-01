### Database

This document discusses how to set up and manage the database used in this
project.

#### Setup

We use [PostgreSQL][postgres] 18 as the only database in this project. Follow
the steps below to set it up:

1. Install PostgreSQL 18 and start the service. Verify the installation by
   running `psql --version`.
2. Create a database named `naisa` by running `createdb naisa`. Create a test
   database named `naisa_test` by running `createdb naisa_test`.
3. Copy `server/src/main/resources/database.example.properties` to
   `database.properties` and `database-test.properties` in the same directory.
4. Set `spring.datasource.username` in both newly created properties files to
   your username, which you can obtain by running
   `psql -d naisa -Atc 'SELECT current_user;'`.

In the `server` workspace, run `make run` (which uses the configuration in
`database.properties`) or `make run-test` (which uses the configuration in
`database-test.properties`). If the service starts successfully, the setup is
complete. If the service fails to start, the setup may still be complete. Check
the logs to see whether the error is related to the database.

#### Drop a Database

To drop a `naisa_test` database, run the following command:

```bash
psql -d postgres -c "DROP DATABASE naisa_test;"
```

[postgres]: https://www.postgresql.org/about/
