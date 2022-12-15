package com._4point.aem.formspipeline.api;

import java.util.Optional;

import com._4point.aem.formspipeline.contexts.MapContext.MapContextBuilder;

public interface Context {
	public final static String FORMSPIPELINE_PROPERTY_PREFIX = "formspipeline.";
	
	/**
	 * Gets an Optional of T for this key. 
	 * 
	 * @param <T>
	 * @param key
	 * @param target
	 * @return
	 */
	<T> Optional<T> get(String key, Class<T> target);

	/**
	 * Gets a String value for this key (if one exists).
	 * 
	 * @param key
	 * @return
	 */
	default Optional<String> getString(String key) { return get(key, String.class); }
	
	/**
	 * Gets a Boolean value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default Optional<Boolean> getBoolean(String key) { return get(key, Boolean.class).or(()->get(key, String.class).map(Boolean::valueOf)); }
	
	/**
	 * Gets a Integer value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default Optional<Integer> getInteger(String key) { return get(key, Integer.class).or(()->get(key, String.class).map(Integer::valueOf)); }

	/**
	 * Gets a Integer value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default Optional<Long> getLong(String key) { return get(key, Long.class).or(()->get(key, String.class).map(Long::valueOf)); }

	/**
	 * Gets a Double value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default Optional<Double> getDouble(String key) { return get(key, Double.class).or(()->get(key, String.class).map(Double::valueOf)); }
	
	public static interface ContextBuilder {
		public ContextBuilder put(String key, Object value);
		public Context build();
	}
}
