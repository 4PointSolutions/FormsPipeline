package com._4point.aem.formspipeline.api;

/**
 * This is a "tagging" interface that is the parent of all the DataTransformation interfaces.
 *
 */
public sealed interface DataTransformation permits DataTransformationOneToOne<? extends DataChunk, ? extends DataChunk>, 
												   DataTransformationOneToMany<? extends DataChunk>, 
												   DataTransformationManyToOne<? extends DataChunk>, 
												   DataTransformationManyToMany 
												   {
}
