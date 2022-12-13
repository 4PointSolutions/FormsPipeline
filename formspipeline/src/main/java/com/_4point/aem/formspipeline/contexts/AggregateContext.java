package com._4point.aem.formspipeline.contexts;

import java.util.Optional;

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
}
