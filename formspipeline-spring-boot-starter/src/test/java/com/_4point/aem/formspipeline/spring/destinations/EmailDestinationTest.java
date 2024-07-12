package com._4point.aem.formspipeline.spring.destinations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.payloads.PdfPayload;
import com._4point.aem.formspipeline.spring.destinations.EmailDestination.ContextWriter;
import com._4point.aem.formspipeline.spring.utils.EmailService;
import com._4point.aem.formspipeline.spring.utils.EmailService.EmailServiceException;
import com._4point.aem.formspipeline.spring.utils.EmailService.SendEmailData;

import jakarta.activation.DataSource;

@ExtendWith(MockitoExtension.class)
class EmailDestinationTest {
	private static final byte[] MOCK_PDF_DATA = "Mock PDF Data".getBytes(StandardCharsets.UTF_8);

	private static final List<String> TO_VALUES = List.of("to1", "to2");
	private static final List<String> CC_VALUES = List.of("cc1", "cc2");
	private static final List<String> BCC_VALUES = List.of("bcc1", "bcc2");
	private static final String FROM_VALUE = "from_value";
	private static final String SUBJECT_VALUE = "subject_value";
	private static final String BODY_VALUE = "body_value";
	private static final String ATTACHMENT_FILENAME_VALUE = "attachment_filename_value";
	
	
	@Captor private ArgumentCaptor<SendEmailData> sendEmailData;
	
	@BeforeEach
	void setUp() throws Exception {
	}
	
	private enum TestScenario {
		ATTACHMENT_WITH_EXPLICIT_NAME(TestScenario::withName, ATTACHMENT_FILENAME_VALUE),
		ATTACHMENT_WITH_DEFAULT_NAME(TestScenario::withoutName, "pdfAttachment")
		;
		
		private final Supplier<Context> contextCreator;
		private final String expectedName;
		
		private TestScenario(Supplier<Context> contextCreator, String expectedName) {
			this.contextCreator = contextCreator;
			this.expectedName = expectedName;
		}

		Context context() { return contextCreator.get(); }
		public String expectedName() { return expectedName; }
		
		private static Context withName() {
			return populateWriter()
					  .attachmentFilename(Path.of(ATTACHMENT_FILENAME_VALUE))
					  .build()
					  ;
		}

		private static Context withoutName() {
			return populateWriter()
					  .build()
					  ;
		}

		private static ContextWriter populateWriter() {
			// Set up SendEmailData using all convenience functions (so that they are exercised as well)
			return EmailDestination.writer()
					  .to(TO_VALUES)
					  .cc(CC_VALUES)
					  .bcc(BCC_VALUES)
					  .from(FROM_VALUE)
					  .subject(SUBJECT_VALUE)
					  .body(BODY_VALUE);
		}
	}

	@ParameterizedTest
	@EnumSource
	void testProcess(TestScenario scenario, @Mock EmailService mockEmailService) throws Exception {
		EmailDestination underTest = new EmailDestination(mockEmailService);
		Mockito.when(mockEmailService.sendMail(sendEmailData.capture())).thenReturn(mockEmailService);
		Context dataContext = scenario.context();
		Message<PdfPayload> input = MessageBuilder.createMessage(new PdfPayload(MOCK_PDF_DATA), dataContext);
		Optional<Message<?>> result = underTest.process(input);
		assertNotNull(result);

		// Validate the result - There's no return
		assertTrue(result.isEmpty());
		
		
		
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
		
		// Validate Attachments
		List<DataSource> attachments = actualSendEmailData.attachments();
		assertNotNull(attachments);
		assertEquals(1, attachments.size(), "Should only have one attachment (i.e. the PDF)");
		DataSource dataSource = attachments.get(0);
		assertAll(
				()->assertArrayEquals(MOCK_PDF_DATA, dataSource.getInputStream().readAllBytes()),
				()->assertEquals("application/pdf", dataSource.getContentType()),
				()->assertEquals(scenario.expectedName(), dataSource.getName())
				);
	}

	@Test
	void testEmailError(@Mock EmailService mockEmailService) throws Exception {
		String expectedMsg = "DummyMessage";
		EmailDestination underTest = new EmailDestination(mockEmailService);
		Mockito.when(mockEmailService.sendMail(sendEmailData.capture())).thenThrow(new EmailServiceException(expectedMsg));
	
		// Set up SendEmailData
		Context dataContext = EmailDestination.writer()
											  .to(TO_VALUES)
											  .cc(CC_VALUES)
											  .bcc(BCC_VALUES)
											  .from(FROM_VALUE)
											  .subject(SUBJECT_VALUE)
											  .body(BODY_VALUE)
											  .attachmentFilename(Path.of(ATTACHMENT_FILENAME_VALUE))
											  .build()
											  ;
		Message<PdfPayload> input = MessageBuilder.createMessage(new PdfPayload(MOCK_PDF_DATA), dataContext);
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->underTest.process(input));
		String msg = ex.getMessage();
		assertNotNull(msg);
		
		assertThat(msg, allOf(containsString(expectedMsg), containsString("Error while sending email")));
	}
}
