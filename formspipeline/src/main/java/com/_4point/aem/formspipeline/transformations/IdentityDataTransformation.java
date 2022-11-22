package com._4point.aem.formspipeline.transformations;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.api.DataTransformationManyToMany;
import com._4point.aem.formspipeline.api.DataTransformationOneToOne;

public class IdentityDataTransformation<T extends DataChunk> implements DataTransformationManyToMany, DataTransformationOneToOne<T, T> {

	@Override
	public T process(T dataChunk) {
		return dataChunk;
	}

	@Override
	public Stream<? extends DataChunk> process(Stream<? extends DataChunk> dataChunks) {
		return dataChunks;
	}

}
