package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface DataTransformationManyToMany extends DataTransformation {
	Stream<? extends DataChunk> process(Stream<? extends DataChunk> dataChunks);
}
