package com._4point.aem.formspipeline.transformations;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationManyToMany;

public class IdentityDataTransformation<T extends DataChunk<? extends Context>> implements DataTransformationManyToMany<T, T>, DataTransformationOneToOne<T, T> {

	@Override
	public T process(T dataChunk) {
		return dataChunk;
	}

	@Override
	public Stream<T> process(Stream<T> dataChunks) {
		return dataChunks;
	}
}
