package com._4point.aem.formspipeline.destinations;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;

@ExtendWith(MockitoExtension.class)
class FolderDestinationTest {

	@TempDir
	private Path testFolder;
	
	@Mock private OutputChunk<EmptyContext, EmptyContext> mockOutputChunk;

	private String mockOutputString = "Test Bytes";
	private byte[] mockOutputBytes = mockOutputString.getBytes(StandardCharsets.UTF_8); 
	
	@Test
	void testProcessSuccess() throws Exception {
		final Path filename = Path.of("foo.txt");
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderDestination<EmptyContext, EmptyContext> underTest = new FolderDestination<>(testFolder, (a, b)->filename);
		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		
		Path expectedFile = testFolder.resolve(filename);
		assertTrue(Files.exists(expectedFile), "Expected file (" + expectedFile.toString() + ") to exist, but it didn't.");
		assertEquals(mockOutputString, Files.readString(expectedFile));
		
		assertNull(result.dataContext());		// Should be null because the mock will not return anything
		assertNull(result.outputContext());		// Should be null because the mock will not return anything
		assertSame(EmptyContext.emptyInstance(), result.resultContext());
	}

	@Test
	void testProcessFailure() {
		final Path filename = Path.of("");	// Intentionally invalid filename
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderDestination<EmptyContext, EmptyContext> underTest = new FolderDestination<>(testFolder, (a, b)->filename);
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->underTest.process(mockOutputChunk));
		String msg = ex.getMessage();
		assertNotNull(msg);
		
		assertThat(msg, allOf(containsString("Unable to write file"), containsString(testFolder.toAbsolutePath().toString())));
	}

}
