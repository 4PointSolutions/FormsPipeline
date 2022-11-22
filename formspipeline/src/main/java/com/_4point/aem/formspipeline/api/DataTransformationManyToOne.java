package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface DataTransformationManyToOne<T extends DataChunk<? extends Context>> extends DataTransformation {
	T process(Stream<? extends DataChunk<? extends Context>> dataChunks);
}
