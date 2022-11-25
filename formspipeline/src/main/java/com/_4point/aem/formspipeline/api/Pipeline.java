package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public interface Pipeline<DCI extends Context, DCO extends Context, OC extends Context, RC extends Context, D extends DataChunk<DCI>> {
	
	public Stream<? extends Result<DCO, OC, RC>> process(D dataChunk);
	public Stream<? extends Result<DCO, OC, RC>> process(Stream<D> dataChunks);

	public interface PipelineFactory<
									DCI extends Context,		// Data Context In
									DCO extends Context,		// Data Context Out
									OC extends Context,		// Output Context
									RC extends Context,		// Result Context
									DI extends DataChunk<DCI>,
									DO extends DataChunk<DCO>,
								    DT extends DataTransformation<DI, DO>, 
								    OG extends OutputGeneration<DO, ? extends OutputChunk<DCO, OC>>,
								    OT extends OutputTransformation, 
								    OD extends OutputDestination<? extends OutputChunk<DCO, OC>,? extends Result<DCO, OC, RC>>
								    > {
		
		PipelineBuilder<DCI, DCO, OC, RC, DI> builder(); 
	
		public interface PipelineBuilder<DCI extends Context, DCO extends Context, OC extends Context, RC extends Context, D extends DataChunk<DCI>> {
			Pipeline<DCI, DCO, OC, RC, D> build();
		}
	}
}

