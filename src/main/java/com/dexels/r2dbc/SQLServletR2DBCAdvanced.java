package com.dexels.r2dbc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reactivestreams.servlet.ResponseSubscriber;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.Connection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SQLServletR2DBCAdvanced extends HttpServlet {

	private static final long serialVersionUID = 4008686226298740688L;
	private ConnectionPool pool;

	private final AtomicLong queryCount = new AtomicLong();
	@Override
	public void destroy() {
		if(this.pool!=null) {
			this.pool.dispose();
		}
	}

	@Override
	public void init() throws ServletException {
	    PostgresqlConnectionConfiguration configuration = PostgresqlConnectionConfiguration.builder()
	            .database("dvdrental")
	            .host("postgres")
	            .password("mysecretpassword")
	            .username("postgres")
	            .build();
	    
	    PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(configuration);

	    ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
	    		   .validationQuery("SELECT 1")
	    		   .maxIdleTime(Duration.ofSeconds(10))
	    		   .maxSize(20)
	    		   .build();

	    pool = new ConnectionPool(poolConfiguration);
	    
	}


	private Mono<Connection> getConnection() {
		return 	pool.create()
		    	.doAfterSuccessOrError(this::disposeConnection); 
	}
	private Mono<String> actorName(Short actorId) {
		return getConnection()
			.flatMapMany(connection->connection.createStatement("select last_name l,first_name f from actor where actor_id = $1")
					.bind(0, actorId)
					.execute())
			.doOnNext(e->queryCount.incrementAndGet())
	    	.flatMap(e->e.map((row,rowmeta)->row.get("l",String.class)+", "+row.get("f",String.class)),1)
	    	.singleOrEmpty();
	}
	
	private Flux<Short> actorsFromFilm(Short filmId) {
		return getConnection()
				.flatMapMany(connection->connection.createStatement("select actor_id from film_actor where film_id = $1").bind(0, filmId).execute())
				.doOnNext(e->queryCount.incrementAndGet())
		    	.flatMap(e->e.map((row,rowmeta)->row.get("actor_id",Short.class)),1);
		
	}
	
	
	private Flux<String> filmDetails(int filmId, String title, String description) {
		return Flux.just("Film with title: "+title+ "\n and description: "+description)
			.concatWith(actorsFromFilm((short)filmId).flatMap(this::actorName))
			.map(name->"  -> Actor by name: "+name+"\n")
			.concatWith(Flux.just("End of film with title: "+title+"\n"));
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		queryCount.set(0);
		ResponseSubscriber subscriber = new ResponseSubscriber(req.startAsync());
		
		getConnection()
			.doOnNext(e->queryCount.incrementAndGet())
	    	.flatMapMany(connection->connection.createStatement("select title t,film_id f,description d from film").execute())
	    	.flatMap(e->e.map((row,rowmeta)->{
	    		String title =  row.get(0,String.class);
	    		Integer filmId = row.get(1,Integer.class);
	    		String description = row.get(2,String.class);
	    		return filmDetails(filmId,title,description);
	    		
	    	}),1)
	    	.flatMap(e->e,1)
	    	.doAfterTerminate(()->System.err.println("Final number of queries: "+queryCount.get()))
	    	.map(String::getBytes)
	    	.map(ByteBuffer::wrap)
	    	.subscribe(subscriber);

	}

	private void disposeConnection(Connection connection, Throwable ex) {
		if(connection!=null) {
			Mono.from(connection.close())
				.subscribe();
		}
	}
	
}
