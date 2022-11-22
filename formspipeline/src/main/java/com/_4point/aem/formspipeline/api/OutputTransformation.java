package com._4point.aem.formspipeline.api;

/**
 * This is a "tagging" interface that is the parent of all the OutputTransformation interfaces.
 *
 */
public sealed interface OutputTransformation permits OutputTransformationOneToOne<? extends OutputChunk, ? extends OutputChunk>,
													 OutputTransformationOneToMany<? extends OutputChunk>,
													 OutputTransformationManyToOne<? extends OutputChunk>,
													 OutputTransformationManyToMany
													 {
}
