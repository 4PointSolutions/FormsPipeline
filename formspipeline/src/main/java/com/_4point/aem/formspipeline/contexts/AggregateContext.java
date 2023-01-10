package com._4point.aem.formspipeline.contexts;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.Context;

/**
 * Class that allows you to aggregate two contexts into a single context.  
 *
 */
public class AggregateContext implements Context {
	private final Context newContext;
	private final Context oldContext;
	
	/**
	 * Create a new Aggregate Context.  Properties in the new context will override properties in the old context.
	 * Properties in the old context that were not overridden are still available as well.
	 * 
	 * @param newContext
	 * @param oldContext
	 */
	public AggregateContext(Context newContext, Context oldContext) {
		this.newContext = newContext;
		this.oldContext = oldContext;
	}

	@Override
	public <T> Optional<T> get(String key, Class<T> target) {
		return newContext.get(key, target).or(()->oldContext.get(key, target));
	}
	
	/**
	 * Aggregate all the contexts passed in into one context.
	 * 
	 * If the same keys exist in multiple contexts, contexts that occur earlier in the argument list have precedence over contexts 
	 * that appear later in the argument list. 
	 * 
	 * @param contexts set of contexts to be aggregated.
	 * @return consolidated Context object
	 */
	public static Context aggregate(Context... contexts) {
		return aggregate(List.of(contexts));
	}

	/**
	 * Aggregate all the contexts passed in into one context.
	 * 
	 * If the same keys exist in multiple contexts, contexts that occur earlier in the argument list have precedence over contexts 
	 * that appear later in the argument list. 
	 * 
	 * @param contexts set of contexts to be aggregated.
	 * @return consolidated Context object
	 */
	public static Context aggregate(List<Context> contexts, Context... otherContexts) {
		return aggregate(Stream.concat(contexts.stream(), Arrays.stream(otherContexts)).toList());
	}

	/**
	 * Aggregate all the contexts passed in into one context.
	 * 
	 * If the same keys exist in multiple contexts, contexts that occur earlier in the argument list have precedence over contexts 
	 * that appear later in the argument list. 
	 * 
	 * @param contexts
	 * @return
	 */
	public static Context aggregate(List<Context> contexts) {
		if (contexts.size() == 0) {
			return EmptyContext.emptyInstance();
		} else if (contexts.size() == 1) {
			return contexts.get(0);
		} else if (contexts.size() == 2) {
			return new AggregateContext(contexts.get(0), contexts.get(1));
		} else {
			return new AggregateContext(contexts.get(0), aggregate(contexts.subList(1, contexts.size())));
		}
	}
}
