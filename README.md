# ADBA and R2DBC demo

A bunch of simple servlets that explore different non-blocking interactions from SQL statement

To build & run locally in Jetty:

```
mvn clean jetty:run
```

It will run a jetty instance try to connect to a postgres database on host 'postgres'.

To use this with an easy to use sample database, there is a docker-compose file.
To use it, simply run:

```
mvn package
```

Which will create the war file

Then enter:

```
docker-compose up --build
```

This should start a demo database and the demo itself.

 - http://localhost:8080/adba-r2dbc-demo/Blocking
 - http://localhost:8080/adba-r2dbc-demo/SQLServletSimpleABDABlocking
 - http://localhost:8080/adba-r2dbc-demo/SQLServletABDABlocking
 - http://localhost:8080/adba-r2dbc-demo/SQLServletABDANonBlocking
 - http://localhost:8080/adba-r2dbc-demo/SQLServletABDANonBlockingBackpressure
 - http://localhost:8080/adba-r2dbc-demo/SQLServletR2DBC

Known issues:

- /SQLServletABDANonBlockingBackpressure does not actually honor backpressure (not implemented in the pgadba driver just yet) so it sort of works but it will drop a few rows depending on relative speeds of the JDBC / servlet connection
