//package com._4point.aem.formspipeline.pipelines;
//
//import java.util.stream.Stream;
//
//import com._4point.aem.formspipeline.api.Context;
//import com._4point.aem.formspipeline.api.DataChunk;
//import com._4point.aem.formspipeline.api.DataTransformation;
//import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationManyToMany;
//import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationManyToOne;
//import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToMany;
//import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
//import com._4point.aem.formspipeline.api.OutputChunk;
//import com._4point.aem.formspipeline.api.OutputDestination;
//import com._4point.aem.formspipeline.api.OutputGeneration;
//import com._4point.aem.formspipeline.api.OutputTransformation;
//import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationManyToMany;
//import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationManyToOne;
//import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationOneToMany;
//import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationOneToOne;
//import com._4point.aem.formspipeline.api.Pipeline;
//import com._4point.aem.formspipeline.api.Pipeline.PipelineFactory;
//import com._4point.aem.formspipeline.api.Result;
//
//public class SequentialPipelineFactory<
//										DCI extends Context,		// Data Context In
//										DCO extends Context,		// Data Context Out
//										OC extends Context,		// Output Context
//										RC extends Context,		// Result Context
//										DI extends DataChunk<DCI>,
//										DO extends DataChunk<DCO>,
//										O extends OutputChunk<DCO, OC>,
//										R extends Result<DCO, OC, RC>,
//										DT extends DataTransformation<DI, DO>, 
//										OG extends OutputGeneration<DO, O>,
//										OT extends OutputTransformation, 
//										OD extends OutputDestination<O,R>
//										> implements PipelineFactory<DCI, DCO, OC, RC, DI, DO, DT, OG, OT, OD> {
//
//	@Override
//	public SequentialPipelineBuilder<DCI, DCO, OC, RC, DI, DO, O, R, DT, OG, OT, OD> builder() {
//		return new SequentialPipelineBuilder<>();
//	}
//
//	public static <
//	DT extends DataTransformation<DI, DO>, 
//	OG extends OutputGeneration<DO, O>,
//	OT extends OutputTransformation, 
//	OD extends OutputDestination<O,R>,
//	R extends Result<DCO, OC, RC>,
//	DI extends DataChunk<DCI>,
//	DO extends DataChunk<DCO>,
//	O extends OutputChunk<DCO, OC>,
//	DCI extends Context,		// Data Context In
//	DCO extends Context,		// Data Context Out
//	OC extends Context,		// Output Context
//	RC extends Context		// Result Context
//	> SequentialPipelineBuilder<DCI,DCO,OC,RC,DI,DO,O,R,DT,OG,OT,OD> builder2() {
//		return new SequentialPipelineBuilder
//				<>();
//	}
//
//	public static class SequentialPipelineBuilder <
//										DCI extends Context,		// Data Context In
//										DCO extends Context,		// Data Context Out
//										OC extends Context,		// Output Context
//										RC extends Context,		// Result Context
//										DI extends DataChunk<DCI>,
//										DO extends DataChunk<DCO>,
//										O extends OutputChunk<DCO, OC>,
//										R extends Result<DCO, OC, RC>,
//										DT extends DataTransformation<DI, DO>, 
//										OG extends OutputGeneration<DO, O>,
//										OT extends OutputTransformation, 
//										OD extends OutputDestination<O,R>
//										> implements PipelineBuilder<DCI, DCO, OC, RC, DI> {
//
//		private DT dataTransformation;
//		private OG outputGeneration;
//		private OT outputTransformation;
//		private OD outputDestination;
//		
//		public SequentialPipelineBuilder<DCI, DCO, OC, RC, DI, DO, O, R, DT, OG, OT, OD> setDataTransformation(DT dataTransformation) {
//			this.dataTransformation = dataTransformation;
//			return this;
//		}
//
//		public SequentialPipelineBuilder<DCI, DCO, OC, RC, DI, DO, O, R, DT, OG, OT, OD> setOutputGeneration(OG outputGeneration) {
//			this.outputGeneration = outputGeneration;
//			return this;
//		}
//
//		public SequentialPipelineBuilder<DCI, DCO, OC, RC, DI, DO, O, R, DT, OG, OT, OD> setOutputTransformation(OT outputTransformation) {
//			this.outputTransformation = outputTransformation;
//			return this;
//		}
//
//		public SequentialPipelineBuilder<DCI, DCO, OC, RC, DI, DO, O, R, DT, OG, OT, OD> setOutputDestination(OD outputDestination) {
//			this.outputDestination = outputDestination;
//			return this;
//		}
//
//		@Override
//		public Pipeline<DCI, DCO, OC, RC, DI> build() {
//			return new SequentialPipeline<>(dataTransformation, outputGeneration, outputTransformation, outputDestination);
//		}
//		
//		
//	}
//
//	private static class SequentialPipeline<
//											DCI extends Context,		// Data Context In
//											OCI extends Context,		// Output Context In
//											DCO extends Context,		// Data Context Out
//											OCO extends Context,		// Output Context Out
//											RC extends Context,			// Result Context
//											DI extends DataChunk<DCI>,
//											DO extends DataChunk<DCO>,
//											OI extends OutputChunk<DCO, OCI>,
//											OO extends OutputChunk<DCO, OCO>,
//											RO extends Result<DCO, OCO, RC>,
//											DT extends DataTransformation<DI, DO>, 
//											OG extends OutputGeneration<DO, OI>,
//											OT extends OutputTransformation<OI, OO>, 
//											OD extends OutputDestination<OO,RO>
//											> implements Pipeline<DCI, DCO, OCO, RC, DI> {
//		private final DT dataTransformation;
//		private final OG outputGeneration;
//		private final OT outputTransformation;
//		private final OD outputDestination;
//		
//		private SequentialPipeline(DT dataTransformation, OG outputGeneration, OT outputTransformation, OD outputDestination) {
//			this.dataTransformation = dataTransformation;
//			this.outputGeneration = outputGeneration;
//			this.outputTransformation = outputTransformation;
//			this.outputDestination = outputDestination;
//		}
//
//		@Override
//		public Stream<RO> process(DI dataChunk) {
//			return process(Stream.of(dataChunk));
//		}
//
//		@Override
//		public Stream<RO> process(Stream<DI> dataChunks) {
//			Stream<DO> dtResult = processDataTransformation(dataChunks);
//			Stream<OI> ogResult = dtResult.map(this.outputGeneration::process);
//			Stream<OO> otResult = processOutputTransformation(ogResult);
//			Stream<RO> results = otResult.map(this.outputDestination::process);
//			return results;
//		}
//		
//		@SuppressWarnings("unchecked")
//		private Stream<DO> processDataTransformation(Stream<DI> dataChunks) {
//			// This could be replaced with switch pattern matching.
//			if (this.dataTransformation instanceof DataTransformationOneToOne) {
//				DataTransformationOneToOne<DI,DO> oneToOne = (DataTransformationOneToOne<DI,DO>)this.dataTransformation;
//				return dataChunks.map(oneToOne::process);
//			} else if (this.dataTransformation instanceof DataTransformationOneToMany) {
//				DataTransformationOneToMany<DI,DO> oneToMany = (DataTransformationOneToMany<DI,DO>)this.dataTransformation;
//				return dataChunks.flatMap(oneToMany::process);
//			} else if (this.dataTransformation instanceof DataTransformationManyToOne) {
//				DataTransformationManyToOne<DI,DO> manyToOne = (DataTransformationManyToOne<DI,DO>)this.dataTransformation;
//				return Stream.of(manyToOne.process(dataChunks));
//			} else if (this.dataTransformation instanceof DataTransformationManyToMany) {
//				DataTransformationManyToMany<DI,DO> manyToMany = (DataTransformationManyToMany<DI,DO>)this.dataTransformation;
//				return manyToMany.process(dataChunks);
//			} else {
//				// This should never happen because the DataTransformation class is sealed.
//				throw new IllegalArgumentException("Unknown type of data transformation (" + this.dataTransformation.getClass().getName() + ").");
//			}
//		}
//		
//		@SuppressWarnings("unchecked")
//		private Stream<OO> processOutputTransformation(Stream<OI> outputChunks) {
//			// This could be replaced with switch pattern matching.
//			if (this.outputTransformation instanceof OutputTransformationOneToOne) {
//				OutputTransformationOneToOne<OI, OO> oneToOne = (OutputTransformationOneToOne<OI, OO>)this.outputTransformation;
//				return outputChunks.map(oneToOne::process);
//			} else if (this.outputTransformation instanceof OutputTransformationOneToMany) {
//				OutputTransformationOneToMany<OI, OO> oneToMany = (OutputTransformationOneToMany<OI, OO>)this.outputTransformation;
//				return outputChunks.flatMap(oneToMany::process);
//			} else if (this.outputTransformation instanceof OutputTransformationManyToOne) {
//				OutputTransformationManyToOne<OI, OO> manyToOne = (OutputTransformationManyToOne<OI, OO>)this.outputTransformation;
//				return Stream.of(manyToOne.process(outputChunks));
//			} else if (this.outputTransformation instanceof OutputTransformationManyToMany) {
//				OutputTransformationManyToMany<OI, OO> manyToMany = (OutputTransformationManyToMany<OI, OO>)this.outputTransformation;
//				return manyToMany.process(outputChunks);
//			} else {
//				// This should never happen because the DataTransformation class is sealed.
//				throw new IllegalArgumentException("Unknown type of output transformation (" + this.outputTransformation.getClass().getName() + ").");
//			}
//		}
//
//	}
//}
