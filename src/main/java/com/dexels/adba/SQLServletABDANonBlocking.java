package com.dexels.adba;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reactivestreams.servlet.ResponseSubscriber;

import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Session;

public class SQLServletABDANonBlocking extends HttpServlet {

	private static final long serialVersionUID = 4008686226298740688L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory").builder()
				.url("jdbc:postgresql://postgres:5432/dvdrental").username("postgres").password("mysecretpassword")
				.build();

		ResponseSubscriber subscriber = new ResponseSubscriber(req.startAsync());
		PublishSubject<String> outputPublisher = PublishSubject.create();
		outputPublisher
			.toFlowable(BackpressureStrategy.BUFFER)
			.map(String::getBytes)
			.map(ByteBuffer::wrap)
			.subscribe(subscriber);
			
		try (Session session = ds.getSession()) {
			session.<PublishSubject<String>>rowOperation("select title from film")
					.collect(() -> outputPublisher, (vd, row) -> outputPublisher.onNext(row.at("title").get(String.class)+"\n"))					
					.submit()
					.getCompletionStage()
					.toCompletableFuture()
					.thenRun(outputPublisher::onComplete);
					
		}
	}

}
