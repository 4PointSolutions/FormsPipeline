package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface DataTransformationManyToMany extends DataTransformation {
	Stream<? extends DataChunk<? extends Context>> process(Stream<? extends DataChunk<? extends Context>> dataChunks);
}
