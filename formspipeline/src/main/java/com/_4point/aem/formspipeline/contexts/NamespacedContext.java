package com._4point.aem.formspipeline.contexts;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;

/**
 * NamespacedContext class - used to ensure uniqueness of key names without having to duplicate
 * the namespace prefix throughout the code.
 * 
 * This class will be used in ContextWriters to ensure that the entries for this context have a common prefix.
 * ContextReaders must prepend the namespace in order to read the values from a NamespacedContext. 
 *
 */
public class NamespacedContext implements Context {
	private static final String DEFAULT_NAMESPACE_SEPARATOR = "."; 
	
	private final String prefix;
	private final Context originalContext;
	
	/**
	 * Create the NamespacedContext.
	 * 
	 * @param namespace  namespace String
	 * @param nsSeparator string used to separate the namespace from the key
	 * @param originalContext original context
	 */
	public NamespacedContext(String namespace, String nsSeparator, Context originalContext) {
		this.prefix = namespace + nsSeparator;
		this.originalContext = originalContext;
	}

	/**
	 * Create the NamespacedContext with a default separator (.)
	 * 
	 * @param namespace namespace String
	 * @param originalContext original context
	 */
	public NamespacedContext(String namespace, Context originalContext) {
		this(namespace, DEFAULT_NAMESPACE_SEPARATOR, originalContext);
	}

	@Override
	public <T> Optional<T> get(String key, Class<T> target) {
		if (key.startsWith(prefix)) {
			return originalContext.get(key.substring(prefix.length()), target);
		}
		return Optional.empty();
	}

}
