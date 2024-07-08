/**
 * 
 */
package com._4point.aem.formspipeline.api;

import java.util.OptionalInt;

/**
 * PagedContext is a context that specifically applies to a chunk whose data has pages 
 * (and more specifically has a number of pages).
 *
 */
public interface PagedData extends TypedData {
	OptionalInt numPages();
}
