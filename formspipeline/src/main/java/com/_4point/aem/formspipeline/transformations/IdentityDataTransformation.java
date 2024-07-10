package com._4point.aem.formspipeline.transformations;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationManyToMany;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.api.Message;

public class IdentityDataTransformation<T> implements DataTransformationManyToMany<Message<T>, Message<T>>, DataTransformationOneToOne<Message<T>, Message<T>> {

	@Override
	public Message<T> process(Message<T> dataChunk) {
		return dataChunk;
	}

	@Override
	public Stream<Message<T>> process(Stream<Message<T>> dataChunks) {
		return dataChunks;
	}
}
