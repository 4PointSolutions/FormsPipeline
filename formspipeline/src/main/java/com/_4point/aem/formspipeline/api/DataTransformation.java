package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.DataTransformation.*;

/**
 * This is a "tagging" interface that is the parent of all the DataTransformation interfaces.
 *
 */
public sealed interface DataTransformation permits DataTransformationOneToOne<? extends DataChunk<? extends Context>, ? extends DataChunk<? extends Context>>, 
												   DataTransformationOneToMany<? extends DataChunk<? extends Context>>, 
												   DataTransformationManyToOne<? extends DataChunk<? extends Context>>, 
												   DataTransformationManyToMany 
												   {
	
	public non-sealed interface DataTransformationOneToOne<T extends DataChunk<? extends Context>, R extends DataChunk<? extends Context>> extends DataTransformation {
		public R process(T dataChunk);
	}

	public non-sealed interface DataTransformationOneToMany<T extends DataChunk<? extends Context>> extends DataTransformation {
		public Stream<? extends DataChunk<? extends Context>> process(T dataChunk);
	}

	public non-sealed interface DataTransformationManyToOne<T extends DataChunk<? extends Context>> extends DataTransformation {
		T process(Stream<? extends DataChunk<? extends Context>> dataChunks);
	}

	public non-sealed interface DataTransformationManyToMany extends DataTransformation {
		Stream<? extends DataChunk<? extends Context>> process(Stream<? extends DataChunk<? extends Context>> dataChunks);
	}
}
