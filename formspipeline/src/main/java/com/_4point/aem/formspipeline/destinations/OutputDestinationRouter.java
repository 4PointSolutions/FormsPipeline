package com._4point.aem.formspipeline.destinations;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;

public class OutputDestinationRouter<T extends Context, U extends Context, R extends Context> 
					implements OutputDestination<OutputChunk<T, U>,Result<T, U, R>> {

	private final List<OutputDestination<OutputChunk<T,U>,Result<T,U,R>>> outputDestinationList;
	private final Function<Context, Integer> aFunction;
	
	@SafeVarargs
	public OutputDestinationRouter(Function<Context,Integer> aFunction, OutputDestination<OutputChunk<T,U>,Result<T,U,R>> ...destinations){
		this.aFunction = aFunction;
		outputDestinationList = Arrays.asList(destinations);
	}	
		
	@Override
	public Result<T, U, R> process(OutputChunk<T, U> outputChunk) {		
		Integer index = aFunction.apply(outputChunk.dataContext());
		OutputDestination<OutputChunk<T, U>, Result<T,U,R>> destination = outputDestinationList.get(index);
		return destination.process(outputChunk);
	}
}
