package com._4point.aem.formspipeline.spring.destinations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.spring.utils.EmailService;
import com._4point.aem.formspipeline.spring.utils.EmailService.EmailServiceException;
import com._4point.aem.formspipeline.spring.utils.EmailService.SendEmailData;

@ExtendWith(MockitoExtension.class)
class EmailDestinationTest {
	private static final byte[] MOCK_PDF_DATA = "Mock PDF Data".getBytes(StandardCharsets.UTF_8);

	private static final List<String> TO_VALUES = List.of("to1", "to2");
	private static final List<String> CC_VALUES = List.of("cc1", "cc2");
	private static final List<String> BCC_VALUES = List.of("bcc1", "bcc2");
	private static final String FROM_VALUE = "from_value";
	private static final String SUBJECT_VALUE = "subject_value";
	private static final String BODY_VALUE = "body_value";
	
	
	@Captor private ArgumentCaptor<SendEmailData> sendEmailData;
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcess(@Mock EmailService mockEmailService) throws Exception {
		EmailDestination underTest = new EmailDestination<>(mockEmailService);
		Mockito.when(mockEmailService.sendMail(sendEmailData.capture())).thenReturn(mockEmailService);
		// Set up SendEmailData
		Context dataContext = EmailDestination.writer()
											  .to(TO_VALUES)
											  .cc(CC_VALUES)
											  .bcc(BCC_VALUES)
											  .from(FROM_VALUE)
											  .subject(SUBJECT_VALUE)
											  .body(BODY_VALUE)
											  .build()
											  ;
		PdfOutputChunk<Context> input = PdfOutputChunk.createSimple(dataContext, MOCK_PDF_DATA);
		Result result = underTest.process(input);
		assertNotNull(result);
		
		// Validate the result - There's no return, so just make sure that we got back what we sent in plus the EmptyContext.
		assertAll(
				()->assertSame(input.dataContext(), result.dataContext()),
				()->assertSame(input.outputContext(), result.outputContext()),
				()->assertSame(EmptyContext.emptyInstance(), result.resultContext())
				);
		
		
		// Validate sendEmailData
		SendEmailData actualSendEmailData = sendEmailData.getValue();
		assertNotNull(actualSendEmailData);
		assertAll(
				()->assertIterableEquals(TO_VALUES, actualSendEmailData.to()),
				()->assertIterableEquals(CC_VALUES, actualSendEmailData.cc()),
				()->assertIterableEquals(BCC_VALUES, actualSendEmailData.bcc()),
				()->assertEquals(FROM_VALUE, actualSendEmailData.from()),
				()->assertEquals(SUBJECT_VALUE, actualSendEmailData.subject()),
				()->assertEquals(BODY_VALUE, actualSendEmailData.bodyContent()),
				()->assertTrue(actualSendEmailData.bodyContentType().isEmpty())
				);
		
	}

	@Test
	void testEmailError(@Mock EmailService mockEmailService) throws Exception {
		String expectedMsg = "DummyMessage";
		EmailDestination underTest = new EmailDestination<>(mockEmailService);
		Mockito.when(mockEmailService.sendMail(sendEmailData.capture())).thenThrow(new EmailServiceException(expectedMsg));
	
		// Set up SendEmailData
		Context dataContext = EmailDestination.writer()
											  .to(TO_VALUES)
											  .cc(CC_VALUES)
											  .bcc(BCC_VALUES)
											  .from(FROM_VALUE)
											  .subject(SUBJECT_VALUE)
											  .body(BODY_VALUE)
											  .build()
											  ;
		PdfOutputChunk<Context> input = PdfOutputChunk.createSimple(dataContext, MOCK_PDF_DATA);
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->underTest.process(input));
		String msg = ex.getMessage();
		assertNotNull(msg);
		
		assertThat(msg, allOf(containsString(expectedMsg), containsString("Error while sending email")));
		
	}

}
