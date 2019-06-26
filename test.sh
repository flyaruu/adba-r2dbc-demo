#!/bin/sh
curl http://localhost:8080/adba-r2dbc-demo/Blocking
curl http://localhost:8080/adba-r2dbc-demo/SQLServletR2DBC
curl http://localhost:8080/adba-r2dbc-demo/SQLServletSimpleADBABlocking
curl http://localhost:8080/adba-r2dbc-demo/SQLServletADBABlocking
curl http://localhost:8080/adba-r2dbc-demo/SQLServletADBABlockingAsync
curl http://localhost:8080/adba-r2dbc-demo/SQLServletADBANonBlocking
curl http://localhost:8080/adba-r2dbc-demo/SQLServletADBANonBlockingBackpressure
curl http://localhost:8080/adba-r2dbc-demo/SQLServletR2DBC
