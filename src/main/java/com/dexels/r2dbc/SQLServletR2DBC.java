package com.dexels.r2dbc;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reactivestreams.servlet.ResponseSubscriber;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;

public class SQLServletR2DBC extends HttpServlet {

	private static final long serialVersionUID = 4008686226298740688L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ResponseSubscriber subscriber = new ResponseSubscriber(req.startAsync());

	    PostgresqlConnectionConfiguration configuration = PostgresqlConnectionConfiguration.builder()
	            .database("dvdrental")
	            .host("postgres")
	            .password("mysecretpassword")
	            .username("postgres")
	            .build();

	    PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(configuration);
	    
	    connectionFactory.create()
	    	.flatMapMany(connection->connection.createStatement("select title from film").execute())
	    	.flatMap(e->e.map((row,rowmeta)->row.get("title",String.class)+"\n"))
	    	.map(String::getBytes)
	    	.map(ByteBuffer::wrap)
	    	.subscribe(subscriber);

	}

}
