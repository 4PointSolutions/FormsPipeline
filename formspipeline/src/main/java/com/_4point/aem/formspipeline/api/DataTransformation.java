package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

/**
 * This is a "tagging" interface that is the parent of all the DataTransformation interfaces.
 *
 */
@SuppressWarnings("rawtypes")
public sealed interface DataTransformation<T extends DataChunk<? extends Context>, R extends DataChunk<? extends Context>> 
												   {
	@FunctionalInterface
	public non-sealed interface DataTransformationOneToOne<T extends DataChunk<? extends Context>, R extends DataChunk<? extends Context>> extends DataTransformation {
		R process(T dataChunk);
	}

	@FunctionalInterface
	public non-sealed interface DataTransformationOneToMany<T extends DataChunk<? extends Context>, R extends DataChunk<? extends Context>> extends DataTransformation {
		Stream<R> process(T dataChunk);
	}

	@FunctionalInterface
	public non-sealed interface DataTransformationManyToOne<T extends DataChunk<? extends Context>, R extends DataChunk<? extends Context>> extends DataTransformation {
		R process(Stream<T> dataChunks);
	}

	@FunctionalInterface
	public non-sealed interface DataTransformationManyToMany<T extends DataChunk<? extends Context>, R extends DataChunk<? extends Context>> extends DataTransformation {
		Stream<R> process(Stream<T> dataChunks);
	}
}
