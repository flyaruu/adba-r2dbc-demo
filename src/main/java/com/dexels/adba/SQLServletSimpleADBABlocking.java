package com.dexels.adba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Session;

public class SQLServletSimpleADBABlocking extends HttpServlet {

	private static final long serialVersionUID = 4008686226298740688L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
				.builder()
				.url("jdbc:postgresql://postgres:5432/dvdrental")
				.username("postgres")
				.password("mysecretpassword")
				.build();
		try (Session session = ds.getSession()) {
			session.<List<String>>rowOperation("select title from film")
					.collect(() -> new ArrayList<>(), 
							(list, row) -> list.add(row.at("title").get(String.class))
						)
					.submit()
					.getCompletionStage()
					.toCompletableFuture()
					.thenAccept(list ->list.forEach(line->{
						try {
							resp.getWriter().write(line+"\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}))
					.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException|ExecutionException|TimeoutException e) {
			e.printStackTrace();
			resp.sendError(500, "whoops");
		}
	}

}
