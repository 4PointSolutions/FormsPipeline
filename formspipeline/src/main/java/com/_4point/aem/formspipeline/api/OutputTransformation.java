package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.OutputTransformation.*;

/**
 * This is a "tagging" interface that is the parent of all the OutputTransformation interfaces.
 *
 */
@SuppressWarnings("rawtypes")
public sealed interface OutputTransformation<T extends OutputChunk<? extends Context, ? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> permits OutputTransformationOneToOne,
													 OutputTransformationOneToMany,
													 OutputTransformationManyToOne,
													 OutputTransformationManyToMany
													 {

	@FunctionalInterface
	public non-sealed interface OutputTransformationOneToOne<T extends OutputChunk<? extends Context, ? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
		R process(T outputChunk);
	}

	@FunctionalInterface
	public non-sealed interface OutputTransformationOneToMany<T extends OutputChunk<? extends Context, ? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
		Stream<R> process(T outputChunk);
	}

	@FunctionalInterface
	public non-sealed interface OutputTransformationManyToOne<T extends OutputChunk<? extends Context, ? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
		R process(Stream<T> outputChunks);
	}

	@FunctionalInterface
	public non-sealed interface OutputTransformationManyToMany<T extends OutputChunk<? extends Context, ? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
		Stream<R> process(Stream<T> outputChunks);
	}

}
