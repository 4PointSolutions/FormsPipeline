package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface DataTransformationOneToMany<T extends DataChunk> extends DataTransformation {
	public Stream<? extends DataChunk> process(T dataChunk);
}
