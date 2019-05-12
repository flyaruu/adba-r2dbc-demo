# ADBA and R2DBC demo

A bunch of simple servlets that explore different non-blocking mechanisms for querying a SQL database
For this example I use a simple postgres database with generated data, you can easily spin it up using
docker-compose.

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
 - http://localhost:8080/adba-r2dbc-demo/SQLServletSimpleADBABlocking
 - http://localhost:8080/adba-r2dbc-demo/SQLServletADBABlocking
 - http://localhost:8080/adba-r2dbc-demo/SQLServletADBANonBlocking
 - http://localhost:8080/adba-r2dbc-demo/SQLServletADBANonBlockingBackpressure
 - http://localhost:8080/adba-r2dbc-demo/SQLServletR2DBC

## Known issues:

### ADBA Backpressure
http://localhost:8080/adba-r2dbc-demo/SQLServletABDANonBlockingBackpressure
does not actually honor backpressure (not implemented in the pgadba driver just yet) so it sort of works but it will drop a few rows depending on relative speeds of the JDBC / servlet connection

You'll probably see some log messages:
```
java.lang.IllegalStateException: failed to offer item to subscriber
 	at org.postgresql.adba.submissions.ProcessorSubmission.lambda$addRow$0(ProcessorSubmission.java:82)
 	at java.base/java.util.concurrent.SubmissionPublisher.retryOffer(SubmissionPublisher.java:445)
```

### R2DBC Concurrent connections
I ran into some bugs when running concurrent queries on R2DBC postgres https://github.com/r2dbc/r2dbc-postgresql/issues/107
You can try that using:
```
curl http://localhost:8080/adba-r2dbc-demo/SQLServletR2DBCAdvanced
```
That will try to query all actors for each film. It will work for a few films, and after that it will fail. Perhaps I'm doing something wrong (but in that case, the error message is pretty unhelpful):
```
java.lang.NumberFormatException: For input string: " M"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
	at java.base/java.lang.Integer.parseInt(Integer.java:638)
	at java.base/java.lang.Integer.parseInt(Integer.java:770)
	at io.r2dbc.postgresql.codec.IntegerCodec.doDecode(IntegerCodec.java:61)
	at io.r2dbc.postgresql.codec.IntegerCodec.doDecode(IntegerCodec.java:32)
```

