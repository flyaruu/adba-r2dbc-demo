FROM tomcat:9.0-jre11
MAINTAINER Frank Lyaruu 
COPY target/adba-r2dbc-demo.war /usr/local/tomcat/webapps/
