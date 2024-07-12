package com._4point.aem.formspipeline.api;

import java.util.function.Function;

public interface Message<T> {

	/**
	 * Return the message payload.
	 */
	T payload();

	/**
	 * Return context (never {@code null} but may be empty).
	 */
	Context context();

	default <R> Message<R> transformPayload(Function<T,R> fn) {
		return MessageBuilder.transformPayload(fn).apply(this);
	}
	
	default Message<T> transformContext(Function<? super Context, ? extends Context> fn) {
		return MessageBuilder.<T>transformContext(fn).apply(this);
	}
}