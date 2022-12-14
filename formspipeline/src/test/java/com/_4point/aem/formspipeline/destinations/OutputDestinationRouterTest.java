package com._4point.aem.formspipeline.destinations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.results.SimpleResult;

@ExtendWith(MockitoExtension.class)
class OutputDestinationRouterTest {	
	
	@Mock private EmptyContext mockContext1;	
	@Mock private OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination1;
	@Mock private SimpleResult<Context, Context, Context> result1;
	
	@Mock private EmptyContext mockContext2;
	@Mock private OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination2;
	@Mock private Result<Context, Context, Context> result2;
	
	@Mock private OutputChunk<Context, Context> mockOutputChunk;
	
	@BeforeEach
	void setup() {		
	}
	
	@Test
	void testContructor() {
		Function<Context,Integer> aFunction = (a)-> 1; 	
		OutputDestinationRouter<Context, Context, Context> router = 
				new OutputDestinationRouter<Context, Context, Context>(aFunction,mockOutputDestination1,mockOutputDestination2);
		assertEquals(2,router.getOutputDestinationList().size());
	}	
	
	@Test
	void testProcess() {
		Mockito.when(mockContext1.getString(Mockito.any())).thenReturn(Optional.of("Data1"));		
		Mockito.when(result1.dataContext()).thenReturn(mockContext1);
		Mockito.when(mockOutputDestination1.process(Mockito.any())).thenReturn(result1);
		
		Function<Context,Integer> aFunction = (a)-> 0; //always returns index 1		
		OutputDestinationRouter<Context, Context, Context> router = 
				new OutputDestinationRouter<Context, Context, Context>(aFunction,mockOutputDestination1,mockOutputDestination2);
		Result<Context,Context, ?> results = router.process(mockOutputChunk);
		assertNotNull(results);
		assertEquals(mockContext1,results.dataContext());
		assertEquals(Optional.of("Data1"),results.dataContext().getString("anyTextWillWork"));
	}	
	
	@Test
	void testProcess_GetSecond() {		
		Mockito.when(mockContext2.getString(Mockito.any())).thenReturn(Optional.of("Data2"));
		Mockito.when(result2.dataContext()).thenReturn(mockContext2);
		Mockito.when(mockOutputDestination2.process(Mockito.any())).thenReturn(result2);

		Function<Context,Integer> aFunction = (a)-> 1; //always returns index 1		
		OutputDestinationRouter<Context, Context, Context> router = 
				new OutputDestinationRouter<Context, Context, Context>(aFunction,mockOutputDestination1,mockOutputDestination2);
		Result<Context,Context, ?> results = router.process(mockOutputChunk);
		assertNotNull(results);
		assertEquals(mockContext2,results.dataContext());
		assertEquals(Optional.of("Data2"),results.dataContext().getString("anyTextWillWork"));
	}	
	
}
