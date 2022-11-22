package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface DataTransformationManyToOne<T extends DataChunk> extends DataTransformation {
	T process(Stream<? extends DataChunk> dataChunks);
}
