package com.dexels.adba;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Result.RowColumn;

public class RowProcessor extends SubmissionPublisher<Result.RowColumn> implements Flow.Processor<Result.RowColumn,Result.RowColumn> {

	private Subscription subscription;
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}
	@Override
	public void onNext(RowColumn item) {
		submit(item);
		this.subscription.request(1);
	}
	@Override
	public void onError(Throwable throwable) {
		throwable.printStackTrace();
	}
	@Override
	public void onComplete() {
		System.err.println("completed");
		close();
	}
}
