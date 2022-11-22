package com._4point.aem.formspipeline.api;

/**
 * This is a "tagging" interface that is the parent of all the OutputTransformation interfaces.
 *
 */
public sealed interface OutputTransformation permits OutputTransformationOneToOne<? extends OutputChunk<? extends Context, ? extends Context>, ? extends OutputChunk<? extends Context, ? extends Context>>,
													 OutputTransformationOneToMany<? extends OutputChunk<? extends Context, ? extends Context>>,
													 OutputTransformationManyToOne<? extends OutputChunk<? extends Context, ? extends Context>>,
													 OutputTransformationManyToMany
													 {
}
