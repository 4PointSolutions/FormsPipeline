package com._4point.aem.formspipeline.api;

import java.util.List;
import java.util.Optional;

import com._4point.aem.formspipeline.contexts.AggregateContext;

/**
 * Contexts are immutable collections of name/value pairs.  They are used to pass information between steps in a forms pipeline.
 * 
 * A Context is built using ContextBuilder objects that acquired from the implementing class.
 *
 */
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
	
	/**
	 * Incorporates (combines) this context with one or more other contexts. If the contexts have entries in 
	 * common then contexts earlier in the argument list have precedence over later ones.  All contexts provided 
	 * have precedence over this one.
	 * 
	 * @param contexts
	 * @return
	 */
	default Context incorporate(Context... contexts) {
		return AggregateContext.aggregate(List.of(contexts), this);
	}
	
	/**
	 * Incorporates (combines) this context with one or more other contexts. If the contexts have entries in 
	 * common then contexts earlier in the list have precedence over later ones.  All contexts in the list 
	 * have precedence over this one.
	 * 
	 * @param contexts
	 * @return
	 */
	default Context incorporate(List<Context> contexts) {
		return AggregateContext.aggregate(contexts, this);
	}
	
	public static interface ContextBuilder {
		public ContextBuilder put(String key, Object value);
		public Context build();
	}
}
