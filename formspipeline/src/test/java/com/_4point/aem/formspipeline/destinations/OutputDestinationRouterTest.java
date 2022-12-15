package com._4point.aem.formspipeline.destinations;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;

@ExtendWith(MockitoExtension.class)
class OutputDestinationRouterTest {	
	
	@Mock private static EmptyContext mockContext1;	
	@Mock private static OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination1;
	@Mock private static Result<Context, Context, Context> mockResult1;
	
	@Mock private static EmptyContext mockContext2;
	@Mock private static OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination2;
	@Mock private static Result<Context, Context, Context> mockResult2;
	
	@Mock private static OutputChunk<Context, Context> mockOutputChunk;

	//Needed because when enum is initialized the mocks are not ready.
	private static Supplier<OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>>> supplierOutputDestination1 = () -> mockOutputDestination1;
	private static Supplier<OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>>> supplierOutputDestination2 = () -> mockOutputDestination2;
	private static Supplier<Result<Context, Context, ?>> supplierResult1 = () -> mockResult1;
	private static Supplier<Result<Context, Context, ?>> supplierResult2 = () -> mockResult2;
	
	enum TestScenarios {
		GET_FIRST_ITEM ((a)-> 0, supplierOutputDestination1, supplierOutputDestination2, supplierResult1),
		GET_SECOND_ITEM ((a)-> 1, supplierOutputDestination1, supplierOutputDestination2, supplierResult2);
		
		Function<Context,Integer> aFunction;
		Supplier<OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>>> destination1;
		Supplier<OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>>> destination2;
		Supplier<Result<Context,Context, ?>> expectedDesults;
		
		private TestScenarios(Function<Context,Integer> func, 
				Supplier<OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>>> dest1,
				Supplier<OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>>> dest2,
				Supplier<Result<Context,Context, ?>> result) {			
			aFunction = func;
			destination1 = dest1;
			destination2 = dest2;	
			expectedDesults = result;
		}
	}
		
	@DisplayName("Test process and execute OutputDestination's process successfully")
	@ParameterizedTest(name="{index}=> TestScenario=''{0}''")
	@EnumSource(TestScenarios.class)
	//this test loops through each enum.
	void testProcess_retrieveElements_Success(TestScenarios scenario) {
		//used lenient here because depending which element is currently being tested the other element won't be checked,
		//this avoids the unnecessary stubbing exception		
		lenient().when(mockOutputDestination1.process(mockOutputChunk)).thenReturn(mockResult1);
		lenient().when(mockOutputDestination2.process(mockOutputChunk)).thenReturn(mockResult2);		

		OutputDestinationRouter<Context, Context, Context> underTest = new OutputDestinationRouter<Context, Context, Context>
				(scenario.aFunction, scenario.destination1.get(),scenario.destination2.get());
		
		Result<Context,Context, ?> results = underTest.process(mockOutputChunk);
		assertNotNull(results);
		assertSame(scenario.expectedDesults.get(), results);
	}	
	
	@SuppressWarnings("unchecked")
	@Test
	void testProcess_throws_IndexOutOfBoundsException() {	
		OutputDestinationRouter<Context, Context, Context> underTest = new OutputDestinationRouter<Context, Context, Context>
		((a)-> 2, Mockito.mock(OutputDestination.class), Mockito.mock(OutputDestination.class));
		
		IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, ()->underTest.process(mockOutputChunk));
		String msg = ex.getMessage();
		assertNotNull(msg);		
		assertThat(msg, allOf(containsString("Index 2 out of bounds for length 2")));
	}	
	
}
