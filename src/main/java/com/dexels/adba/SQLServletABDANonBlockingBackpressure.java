package com.dexels.adba;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reactivestreams.servlet.ResponseSubscriber;

import hu.akarnokd.rxjava2.interop.FlowInterop;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Session;
public class SQLServletABDANonBlockingBackpressure extends HttpServlet {

	private static final long serialVersionUID = 4008686226298740688L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory").builder()
				.url("jdbc:postgresql://postgres:5432/dvdrental").username("postgres").password("mysecretpassword")
				.build();
		ResponseSubscriber subscriber = new ResponseSubscriber(req.startAsync());

		RowProcessor rp = new RowProcessor();
		CompletableFuture<String> result = new CompletableFuture<>();

		try (Session session = ds.getSession()) {
			final CompletableFuture<String> completableFuture = session
					.<String>rowPublisherOperation("select title from film")
					.subscribe(rp, result)
					.submit()
					.getCompletionStage()
					.toCompletableFuture();
	
			FlowInterop.fromFlowPublisher(rp)
					.doOnTerminate(session::close)
					.map(e -> (String) e.at("title").get()+"\n")
					.map(String::getBytes)
					.map(ByteBuffer::wrap)
					.subscribe(subscriber);
		}
	}

}
