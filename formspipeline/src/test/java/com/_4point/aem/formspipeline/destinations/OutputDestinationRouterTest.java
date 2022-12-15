package com._4point.aem.formspipeline.destinations;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
	
	@Mock private EmptyContext mockContext1;	
	@Mock private OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination1;
	@Mock private Result<Context, Context, Context> mockResult1;
	
	@Mock private EmptyContext mockContext2;
	@Mock private OutputDestination<OutputChunk<Context, Context>,Result<Context, Context, Context>> mockOutputDestination2;
	@Mock private Result<Context, Context, Context> mockResult2;
	
	@Mock private OutputChunk<Context, Context> mockOutputChunk;

	
	@DisplayName("Test process and execute OutputDestination's process successfully")
	@ParameterizedTest(name="Selection function returns ''{0}''")
	@ValueSource(ints = { 0, 1 })
	void testProcess_retrieveElements_Success(int index) {
		List<Result<Context, Context, Context>> expectedResults = List.of(mockResult1, mockResult2);
		//used lenient here because depending which element is currently being tested the other element won't be checked,
		//this avoids the unnecessary stubbing exception		
		lenient().when(mockOutputDestination1.process(mockOutputChunk)).thenReturn(mockResult1);
		lenient().when(mockOutputDestination2.process(mockOutputChunk)).thenReturn(mockResult2);		

		var underTest = new OutputDestinationRouter<Context, Context, Context>(c->index, mockOutputDestination1,mockOutputDestination2);
		
		Result<Context,Context, ?> results = underTest.process(mockOutputChunk);
		
		assertNotNull(results);
		assertSame(expectedResults.get(index), results);
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
