package com._4point.aem.formspipeline.destinations;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;

@ExtendWith(MockitoExtension.class)
class OutputDestinationRouterTest {	
	
	private static EmptyContext mockContext1;	
	private static OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination1;
	private static Result<Context, Context, Context> mockResult1;
	
	private static EmptyContext mockContext2;
	private static OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination2;
	private static Result<Context, Context, Context> mockResult2;
	
	private static OutputChunk<Context, Context> mockOutputChunk;
	
	enum TestScenarios {
		GET_FIRST_ITEM ((a)-> 0, mockOutputDestination1, mockOutputDestination2, mockResult1),
		GET_SECOND_ITEM ((a)-> 1, mockOutputDestination1, mockOutputDestination2, mockResult2);
		//GET_OUTOFBOUND_ITEM ((a)-> 2, mockOutputDestination1, mockOutputDestination2, null);
		
		Function<Context,Integer> aFunction;
		OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> destination1;
		OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> destination2;
		Result<Context,Context, ?> expectedDesults;
		
		private TestScenarios(Function<Context,Integer> func, 
				OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> dest1,
				OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> dest2,
				Result<Context,Context, ?> result) {			
			aFunction = func;
			destination1 = dest1;
			destination2 = dest2;	
			expectedDesults = result;
		}
	}
		
	@BeforeAll	
	@SuppressWarnings("unchecked")
	static void mockSetup() {
		mockOutputChunk = Mockito.mock(OutputChunk.class);
		
		mockContext1 = Mockito.mock(EmptyContext.class);
		mockResult1 = Mockito.mock(Result.class);
		mockOutputDestination1 = Mockito.mock(OutputDestination.class);
		Mockito.when(mockContext1.getString(Mockito.anyString())).thenReturn(Optional.of("Data1"));		
		Mockito.when(mockResult1.dataContext()).thenReturn(mockContext1);
		Mockito.when(mockOutputDestination1.process(mockOutputChunk)).thenReturn(mockResult1);

		mockContext2 = Mockito.mock(EmptyContext.class);
		mockResult2 = Mockito.mock(Result.class);
		mockOutputDestination2 = Mockito.mock(OutputDestination.class);
		Mockito.when(mockContext2.getString(Mockito.anyString())).thenReturn(Optional.of("Data2"));
		Mockito.when(mockResult2.dataContext()).thenReturn(mockContext2);
		Mockito.when(mockOutputDestination2.process(mockOutputChunk)).thenReturn(mockResult2);		
	}	
	
	@DisplayName("Test execute PROCESS for each OutputDestination successfully")
	@ParameterizedTest(name="{index}=> TestScenario=''{0}''")
	@EnumSource(TestScenarios.class)
	void testProcess_retrieveElements_Success(TestScenarios scenario) {
		OutputDestinationRouter<Context, Context, Context> underTest = new OutputDestinationRouter<Context, Context, Context>
				(scenario.aFunction, scenario.destination1,scenario.destination2);
		
		Result<Context,Context, ?> results = underTest.process(mockOutputChunk);
		assertNotNull(results);
		assertSame(scenario.expectedDesults.dataContext(),results.dataContext());		
		assertSame(scenario.expectedDesults.dataContext().getString("t"),results.dataContext().getString("anyTextWillWork"));	
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
