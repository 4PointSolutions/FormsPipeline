package com._4point.aem.formspipeline.api;

public interface Message<T> {

	/**
	 * Return the message payload.
	 */
	T payload();

	/**
	 * Return context (never {@code null} but may be empty).
	 */
	Context context();

}