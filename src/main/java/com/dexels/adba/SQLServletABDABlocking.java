package com.dexels.adba;

import java.io.IOException;
import java.io.Writer;
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

public class SQLServletABDABlocking extends HttpServlet {

	private static final long serialVersionUID = 4008686226298740688L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory").builder()
				.url("jdbc:postgresql://postgres:5432/dvdrental").username("postgres").password("mysecretpassword")
				.build();
 
		Writer outputWriter = resp.getWriter();
		try (Session session = ds.getSession()) {
			session.<Writer>rowOperation("select title from film")
					.collect(() -> outputWriter, (writer, row) -> {
						try {
							writer.write(row.at("title").get(String.class)+"\n");
						} catch (IOException e) {}
					})
					.submit()
					.getCompletionStage()
					.toCompletableFuture()
					.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException|ExecutionException|TimeoutException e) {
			throw new ServletException("whoops", e);
		}
	}

}
