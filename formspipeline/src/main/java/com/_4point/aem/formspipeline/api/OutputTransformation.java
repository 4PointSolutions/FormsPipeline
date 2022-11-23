package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.OutputTransformation.*;

/**
 * This is a "tagging" interface that is the parent of all the OutputTransformation interfaces.
 *
 */
public sealed interface OutputTransformation permits OutputTransformationOneToOne<? extends OutputChunk<? extends Context, ? extends Context>, ? extends OutputChunk<? extends Context, ? extends Context>>,
													 OutputTransformationOneToMany<? extends OutputChunk<? extends Context, ? extends Context>>,
													 OutputTransformationManyToOne<? extends OutputChunk<? extends Context, ? extends Context>>,
													 OutputTransformationManyToMany
													 {

	public non-sealed interface OutputTransformationOneToOne<T extends OutputChunk<? extends Context, ? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
		public R process(T outputChunk);
	}

	public non-sealed interface OutputTransformationOneToMany<T extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
		public Stream<? extends OutputChunk<? extends Context, ? extends Context>> process(T outputChunk);
	}

	public non-sealed interface OutputTransformationManyToOne<T extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
		T process(Stream<? extends OutputChunk<? extends Context, ? extends Context>> outputChunks);
	}

	public non-sealed interface OutputTransformationManyToMany extends OutputTransformation {
		Stream<? extends OutputChunk<? extends Context, ? extends Context>> process(Stream<? extends OutputChunk<? extends Context, ? extends Context>> outputChunks);
	}

}
