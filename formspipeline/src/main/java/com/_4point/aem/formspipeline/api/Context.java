package com._4point.aem.formspipeline.api;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com._4point.aem.formspipeline.contexts.AggregateContext;
import com._4point.aem.formspipeline.contexts.NamespacedContext;

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
	 * Gets an List of T for this key.
	 * 
	 * Note: Many types of contexts can only contain a single value for each key.  These contexts do not need to override
	 * the default implementation.  Context implementations that can contain multiple values for a particular key *must*
	 * implement this method.
	 *  
	 * @param <T>
	 * @param key
	 * @param target
	 * @return
	 */
	default <T> List<T> getMulti(String key, Class<T> target) { return get(key, target).map(List::of).orElse(List.of()); };
	
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
	 * Gets a String value for this key (if one exists).
	 * 
	 * @param key
	 * @return
	 */
	default List<String> getStrings(String key) { return getMulti(key, String.class); }
	
	/**
	 * Gets a Boolean value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default List<Boolean> getBooleans(String key) { return or(getMulti(key, Boolean.class), ()->getMulti(key, String.class).stream().map(Boolean::valueOf).toList()); }
	
	/**
	 * Gets a Integer value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default List<Integer> getIntegers(String key) { return or(getMulti(key, Integer.class), ()->getMulti(key, String.class).stream().map(Integer::valueOf).toList()); }

	/**
	 * Gets a Integer value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default List<Long> getLongs(String key) { return or(getMulti(key, Long.class), ()->getMulti(key, String.class).stream().map(Long::valueOf).toList()); }

	/**
	 * Gets a Double value for this key.  The value can be stored as a Boolean or as a String.
	 * 
	 * @param key
	 * @return
	 */
	default List<Double> getDoubles(String key) { return or(getMulti(key, Double.class), ()->getMulti(key, String.class).stream().map(Double::valueOf).toList()); }
	
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
	
	/**
	 * Incorporates (combines) this context with one or more other contexts that will get namespaced to the provided namespace.
	 * If the contexts have entries in common then contexts earlier in the argument list have precedence over later ones.
	 * All contexts provided have precedence over this one.
	 * 
	 * @param namespace
	 * @param contexts
	 * @return
	 */
	default Context incorporate(String namespace, Context... contexts) {
		List<Context> namespacedContexts = Arrays.stream(contexts).map(c->new NamespacedContext(namespace, c)).map(Context.class::cast).toList();
		return AggregateContext.aggregate(namespacedContexts, this);
	}
	
	/**
	 * Incorporates (combines) this context with one or more other contexts that will get namespaced to the provided namespace.
	 * If the contexts have entries in common then contexts earlier in the list have precedence over later ones.  
	 * All contexts in the list have precedence over this one.
	 * 
	 * @param namespace
	 * @param contexts
	 * @return
	 */
	default Context incorporate(String namespace, List<Context> contexts) {
		List<Context> namespacedContexts = contexts.stream().map(c->new NamespacedContext(namespace, c)).map(Context.class::cast).toList();
		return AggregateContext.aggregate(namespacedContexts, this);
	}
	
	private <E> List<E> or(List<E> inList, Supplier<List<E>> supplier) {
		return inList.isEmpty() ? supplier.get() : inList;
	}
	
	public static interface ContextBuilder {
		public ContextBuilder put(String key, Object value);
		public Context build();
	}
}
