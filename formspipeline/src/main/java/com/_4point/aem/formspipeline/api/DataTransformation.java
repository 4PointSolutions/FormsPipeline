package com._4point.aem.formspipeline.api;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is a "tagging" interface that is the parent of all the DataTransformation interfaces.
 *
 */
@SuppressWarnings("rawtypes")
public sealed interface DataTransformation<T extends Message<?>, R extends Message<?>> 
												   {
	@FunctionalInterface
	public non-sealed interface DataTransformationOneToOne<T extends Message<?>, R extends Message<?>> extends DataTransformation {
		R process(T dataChunk);
	}

	@FunctionalInterface
	public non-sealed interface DataTransformationOneToMany<T extends Message<?>, R extends Message<?>> extends DataTransformation {
		Stream<R> process(T dataChunk);
	}

	@FunctionalInterface
	public non-sealed interface DataTransformationManyToOne<T extends Message<?>, R extends Message<?>> extends DataTransformation {
		R process(Stream<T> dataChunks);
	}

	@FunctionalInterface
	public non-sealed interface DataTransformationManyToMany<T extends Message<?>, R extends Message<?>> extends DataTransformation {
		Stream<R> process(Stream<T> dataChunks);
	}

	@FunctionalInterface
	public non-sealed interface DataTransformationOneToOneOrZero<T extends Message<?>, R extends Message<?>> extends DataTransformation {
		Optional<R> process(T dataChunk);
	}


	@FunctionalInterface
	public non-sealed interface DataTransformationManyToOneOrZero<T extends Message<?>, R extends Message<?>> extends DataTransformation {
		Optional<R> process(Stream<T> dataChunks);
	}
}
