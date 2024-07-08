package com._4point.aem.formspipeline.destinations;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.PagedData;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.chunks.PdfPayload;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.contexts.SingletonContext;
import com._4point.aem.formspipeline.utils.JavaPrinterService;

@ExtendWith(MockitoExtension.class)
class LocalPrintDestinationTest {

	private static final byte[] MOCK_PDF_DATA = "Mock PDF Data".getBytes(StandardCharsets.UTF_8);
	private static final String MOCK_PRINT_JOB_NAME = "ExpectedPrintJobName";
	
	@Captor ArgumentCaptor<byte[]> actualBytes;
	@Captor ArgumentCaptor<String> actualPrintJobName;
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcess(@Mock JavaPrinterService mockPrinterService) {
		tester(mockPrinterService)
			.setPrintJobName(MOCK_PRINT_JOB_NAME)
			.process()
			.mockPrinterServiceShouldReceive(MOCK_PDF_DATA, MOCK_PRINT_JOB_NAME);
	}

	@Test
	void testProcess_NoPrintJobName(@Mock JavaPrinterService mockPrinterService) {
		tester(mockPrinterService)
			.setNoPrintJobName()
			.process()
			.mockPrinterServiceShouldReceive(MOCK_PDF_DATA, "AEM Print Job");
	}

	private Tester tester(JavaPrinterService mockPrinterService) { return new Tester(mockPrinterService); }
	
	private class Tester {
		private final JavaPrinterService mockPrinterService;
		private Optional<String> printJobName;
		
		Tester setPrintJobName(String printJobname) {
			this.printJobName = Optional.of(printJobname);
			return this;
		}
		
		Tester setNoPrintJobName() {
			this.printJobName = Optional.empty();
			return this;
		}
		
		public Tester(JavaPrinterService mockPrinterService) {
			this.mockPrinterService = mockPrinterService;
		}

		Asserter process() {
			Context outputContext = printJobName.map(s->LocalPrintDestination.addPrintJobNameToContext(EmptyContext.emptyInstance(), s))
												.orElse(EmptyContext.emptyInstance());
			PdfPayload<Context> input = PdfPayload.createSimple(outputContext, MOCK_PDF_DATA);
			var underTest = new LocalPrintDestination(mockPrinterService);
			Mockito.doNothing().when(mockPrinterService).print(actualBytes.capture(), actualPrintJobName.capture());
			Result result = underTest.process(input);
			assertNotNull(result);
			return new Asserter(result);
		}
		
		private class Asserter {
			Result<? extends Context, ? extends PagedData, ? extends Context> result;
			
			public Asserter(Result<? extends Context, ? extends PagedData, ? extends Context> result) {
				this.result = result;
			}

			void mockPrinterServiceShouldReceive(byte[] expectedBytes, String expectedPrintJobname) {
				assertArrayEquals(expectedBytes, actualBytes.getValue());
				assertEquals(expectedPrintJobname, actualPrintJobName.getValue());
				
			}
		}
	}
}
