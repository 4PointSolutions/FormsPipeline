package com._4point.aem.formspipeline.contexts;

import java.util.Optional;
import java.util.OptionalInt;

import com._4point.aem.formspipeline.api.PagedContext;

/**
 * This class an implementation of a PagedContext which is one that contains pages.
 * 
 * It can be used as a partial implementation by any implementation that implements PagedContext. 
 *
 */
public abstract class AbstractPagedContext implements PagedContext {
	OptionalInt numPages;
	
	public AbstractPagedContext(int numPages) {
		this.numPages = OptionalInt.of(numPages);
	}

	public AbstractPagedContext() {
		this.numPages = OptionalInt.empty();
	}

	@Override
	public abstract <T> Optional<T> get(String key, Class<T> target);

	@Override
	public OptionalInt numPages() {
		return numPages;
	}

}
