package com._4point.aem.formspipeline.spring.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.MultipleFailuresError;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com._4point.aem.formspipeline.spring.utils.EmailService.SendEmailData;
import com._4point.aem.formspipeline.spring.utils.EmailService.SendEmailData.BodyContentType;
import com._4point.aem.formspipeline.spring.utils.EmailService.SimpleSendEmailData;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

public class EmailServiceTest {

	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP).withPerMethodLifecycle(false);
	
	private static JavaMailSender javaMailSender;

	@BeforeAll
	@Order(Integer.MAX_VALUE)	// Must happen after GreenMail Extension starts
	static void setUpBeforeClass() throws Exception {
		javaMailSender = createJavaMailSender();
	}

	@AfterEach
	void tearDown() throws Exception {
		greenMail.purgeEmailFromAllMailboxes();
	}

	@Test
	void testSendMailMinimal() throws Exception {
		final String fromAddress = "FromAddress@example.com";
		final String toAddress = "ToAddress@example.com";
		final String subjectText = "Test email Subject";
		final String bodyText = "Test email body text.";
		SendEmailData emailData = SimpleSendEmailData.getBuilder(List.of(toAddress),
				fromAddress, subjectText, bodyText).build();

		EmailService emailService = EmailService.from(javaMailSender);

		emailService.sendMail(emailData);

		final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		assertEquals(1, receivedMessages.length);
		final MimeMessage receivedMessage = receivedMessages[0];
		assertAll(
				()->assertArrayEquals(new String[] { fromAddress }, convertAddressArrayToStringArray(receivedMessage.getFrom())),
				()->assertEquals(subjectText, receivedMessage.getSubject()),
				()->assertArrayEquals(new String[] { toAddress }, convertAddressArrayToStringArray(receivedMessage.getAllRecipients())),
				()->assertEquals(bodyText, MimeMessageContent.getContentFromMessage(receivedMessage).getBodyText().strip())
			);
	}

	private static JavaMailSenderImpl createJavaMailSender() throws MessagingException {
		var javaMailSenderImpl = new JavaMailSenderImpl();
		javaMailSenderImpl.setPort(3025);
		javaMailSenderImpl.setProtocol("smtp");
		javaMailSenderImpl.setHost("localhost");

		javaMailSenderImpl.testConnection();
		return javaMailSenderImpl;
	}

	public static class MimeMessageContent {
		private static final String MULTIPART_WILDCARD = "multipart/*";
		private static final String TEXT_HTML = "text/html";
		private static final String TEXT_PLAIN = "text/plain";
		private final String bodyText;
		private final String bodyContentType;
		private final List<DataSource> attachments;
		private MimeMessageContent(String bodyText, String bodyContentType, List<DataSource> attachments) {
			super();
			this.bodyText = bodyText;
			this.bodyContentType = bodyContentType;
			this.attachments = List.copyOf(attachments);	// Make defensive unmodifiable copy.
		}
		public String getBodyText() {
			return bodyText;
		}
		public List<DataSource> getAttachments() {
			return attachments;
		}
		public String getBodyContentType() {
			return bodyContentType;
		}
		
		public static MimeMessageContent getContentFromMessage(Message message) throws MessagingException, IOException {
			MimeMessageContent result = null;
		    if (message.isMimeType(TEXT_PLAIN)) {
		        result = new MimeMessageContent(message.getContent().toString(), TEXT_PLAIN, Collections.emptyList());
		    } else if (message.isMimeType(MULTIPART_WILDCARD)) {
		        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
		        result = getContentFromMimeMultipart(mimeMultipart);
		    }
		    return result;
		}
		
		public static MimeMessageContent getContentFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException{
		    String bodyText = "";
		    String bodyContentType = "";
		    List<DataSource> attachments = new ArrayList<>();
		    int count = mimeMultipart.getCount();
		    for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
//		        System.out.println("BodyPart ContentType = '" + bodyPart.getContentType() + "'.");
//		        System.out.println("BodyPart Description = '" + bodyPart.getDescription() + "'.");
//		        System.out.println("BodyPart Disposition = '" + bodyPart.getDisposition() + "'.");
//		        System.out.println("BodyPart Filename = '" + bodyPart.getFileName() + "'.");
				if ("attachment".equals(bodyPart.getDisposition())) {
					final InputStream inputStream = bodyPart.getInputStream();
//					final InputStream inputStream2 = (InputStream)bodyPart.getContent();
					attachments.add(createDataSource(inputStream,bodyPart.getContentType(),bodyPart.getFileName())); 
				} else if (bodyPart.isMimeType(TEXT_PLAIN)) {
					bodyContentType = TEXT_PLAIN;
		            bodyText = bodyText + "\n" + bodyPart.getContent();
		            break; // without break same text appears twice in my tests
		        } else if (bodyPart.isMimeType(TEXT_HTML)) {
					bodyContentType = TEXT_HTML;
		            String html = (String) bodyPart.getContent();
		            bodyText = bodyText + html; // "\n" + org.jsoup.Jsoup.parse(html).text();
		        } else if (bodyPart.getContent() instanceof MimeMultipart){
		            final MimeMessageContent subContent = getContentFromMimeMultipart((MimeMultipart)bodyPart.getContent());
					bodyText = bodyText + subContent.getBodyText();
					bodyContentType = subContent.getBodyContentType();
					attachments.addAll(subContent.getAttachments());
		        } else {
		        	System.out.println("Found unknown body part.");
			        System.out.println("BodyPart ContentType = '" + bodyPart.getContentType() + "'.");
			        System.out.println("BodyPart Description = '" + bodyPart.getDescription() + "'.");
			        System.out.println("BodyPart Disposition = '" + bodyPart.getDisposition() + "'.");
			        System.out.println("BodyPart Filename = '" + bodyPart.getFileName() + "'.");
		        }
		    }
		    return new MimeMessageContent(bodyText, bodyContentType, attachments);
		}
		
	}

	@Test
	void testSendMailMaximal() throws Exception {
		final String fromAddress = "FromAddress1@example.com";
		final List<String> toAddresses = List.of("ToAddress1@example.com", "ToAddress2@example.com", "ToAddress3@example.com");
		final String subjectText = "Test email Subject";
		final String bodyText = "<html><head><meta http-equiv=Content-Type content=\"text/html; charset=utf-8\"></head><body><p>Test email body text.</p></body></html>";
		final List<String> ccAddresses = List.of("CcAddress1@example.com", "CcAddress2@example.com");
		final List<String> bccAddresses = List.of("BccAddress1@example.com", "BccAddress2@example.com", "BccAddress3@example.com");
		final String bodyContentType = "text/html";
		final byte[] documentOfRecordData = "DocumentOfRecordData".getBytes();
		final String dorContentType = "application/pdf";
		final String dorName = "DocumentOfRecord.pdf";
		final byte[] submittedXmlData = "<foo><bar>dsfdsfdsf</bar></foo>".getBytes();
		final String xmlContentType = "application/xml";
		final String submittedXmlName = "submittedData.xml";

		final DataSource documentOfRecordContent = SendEmailData.toDataSource(dorName, documentOfRecordData, dorContentType);
		final DataSource submittedXmlDataContent = SendEmailData.toDataSource(submittedXmlName, submittedXmlData, xmlContentType);
		
		SendEmailData emailData = SimpleSendEmailData.getBuilder(toAddresses, fromAddress, subjectText, bodyText)
				.cc(ccAddresses)
				.bcc(bccAddresses)
				.bodyContentType(bodyContentType)
				.attachment(documentOfRecordContent)
				.attachment(submittedXmlDataContent)
				.build();

		EmailService emailService = EmailService.from(javaMailSender);

			
		emailService.sendMail(emailData);

		final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		assertEquals(toAddresses.size() + ccAddresses.size() + bccAddresses.size(), receivedMessages.length);
		final MimeMessage receivedMessage = receivedMessages[0];
		List<String> allVisibleAddresses = new ArrayList<>(toAddresses);
		allVisibleAddresses.addAll(ccAddresses);
		assertAll(
				()->assertArrayEquals(new String[] { fromAddress }, convertAddressArrayToStringArray(receivedMessage.getFrom())),
				()->assertEquals(subjectText, receivedMessage.getSubject()),
				()->assertArrayEquals(allVisibleAddresses .toArray(), convertAddressArrayToStringArray(receivedMessage.getAllRecipients())),
//				()->assertThat(receivedMessage.getContentType(), containsString(bodyContentType)),
				()->{
					// Make sure all the attachments match.
					final List<DataSource> attachmentsFound = MimeMessageContent.getContentFromMessage(receivedMessage).getAttachments();
					assertAll(
							()->assertEquals(2, attachmentsFound.size()),
							()->assertAttachmentEquals(documentOfRecordContent, attachmentsFound.get(0)),
							()->assertAttachmentEquals(submittedXmlDataContent, attachmentsFound.get(1))
							);
				},
				()->assertEquals(bodyText, MimeMessageContent.getContentFromMessage(receivedMessage).getBodyText().strip())	// Had to strip because there was a LF appended.
		);
		
	}

	@ParameterizedTest
	@EnumSource
	void testBodyContentTypeOf(BodyContentType type) throws Exception {
		final Optional<BodyContentType> returnedType = BodyContentType.of(type.getContentTypeString());
		assertTrue(returnedType.isPresent());
		assertEquals(type, returnedType.get());
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings={"foo"})
	void testBodyContentTypeOfInvalid(String value) throws Exception {
		assertTrue(BodyContentType.of(value).isEmpty());
		
	}

	private enum CommaSeparatedScenario {
		FOOBAR_NOSPACES("foo,bar", List.of("foo", "bar")),
		FOOBAR_WITHSPACES(" foo , bar ", List.of("foo", "bar")),
		FOOBAR_CONTAINING_SPACES("foo bar, bar foo ", List.of("foo bar", "bar foo")),
		EMPTY_ENTRIES("foo bar,,, ,  ,bar foo ", List.of("foo bar","","","","","bar foo")),
		NULL(null, Collections.emptyList()),
		EMPTY("", Collections.emptyList());
		
		private final String testValue;
		private final List<String> expectedResult;
		private CommaSeparatedScenario(String testValue, List<String> expectedResult) {
			this.testValue = testValue;
			this.expectedResult = expectedResult;
		}
	};
	
	@ParameterizedTest
	@EnumSource
	void testConvertCommaSeparated(CommaSeparatedScenario scenario) throws Exception {
		final List<String> result = EmailService.SendEmailData.convertCommaSeparated(scenario.testValue);
		assertEquals(scenario.expectedResult.size(), result.size());
		for (int i = 0; i < result.size(); i++) {
			assertEquals(scenario.expectedResult.get(i), result.get(i));
		}
	}

	public static DataSource createDataSource(final InputStream submittedXmlStream, final String xmlContentType, final String submittedXmlName) throws IOException {
		return EmailService.SendEmailData.toDataSource(submittedXmlName, submittedXmlStream.readAllBytes(), xmlContentType);
	}

	private static void assertAttachmentEquals(DataSource expected, DataSource actual) {
		assertAll(
				()->assertEquals(expected.getContentType(), actual.getContentType().split(";")[0]),	// remove any trailing filename.
				()->assertEquals(expected.getName(), actual.getName()),
				()->assertArrayEquals(expected.getInputStream().readAllBytes(), actual.getInputStream().readAllBytes())
				);
	}
	
	private static String[] convertAddressArrayToStringArray(final Address[] var1) {
		return Arrays.stream(var1).map(Address::toString).toArray(String[]::new);
	}

	@Nested
	static class SimpleSendEmailDataTests {
		private static final String SAMPLE_TO = "foo@example.com";
		private static final String SAMPLE_FROM = "bar@4point.com";
		private static final String SAMPLE_CC = "cc@example.com";
		private static final String SAMPLE_BCC = "bcc@example.com";
		private static final String SAMPLE_BODY_CONTENT = "Fake Body Content";
		private final BodyContentType SAMPLE_CONTENT_TYPE = BodyContentType.HTML_TEXT;
		private static final String SAMPLE_SUBJECT = "Fake Subject";

		@Test
		void testMandatoryFields() {
			// Test that an exception is thrown if a mandatory field is null
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(null, SAMPLE_FROM, SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT).build(), "To", "cannot be null");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), null, SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT).build(), "From", "cannot be null");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, null, SAMPLE_BODY_CONTENT).build(), "Subject", "cannot be null");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, SAMPLE_SUBJECT, null).build(), "Body", "cannot be null");
			// Test that an exception is thrown if a mandatory field is empty
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(""), SAMPLE_FROM, SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT).build(), "To", "cannot be empty");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), "", SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT).build(), "From", "cannot be blank");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, "", SAMPLE_BODY_CONTENT).build(), "Subject", "cannot be blank");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, SAMPLE_SUBJECT, "").build(), "Body", "cannot be blank");
			// Test that an exception is thrown if a mandatory field is blank
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of("", " ", "   "), SAMPLE_FROM, SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT).build(), "To", "cannot be empty");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), " ", SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT).build(), "From", "cannot be blank");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, " ", SAMPLE_BODY_CONTENT).build(), "Subject", "cannot be blank");
			assertThrowsAndMsgContains(IllegalArgumentException.class, ()->SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, SAMPLE_SUBJECT, " ").build(), "Body", "cannot be blank");
		}
		
		@Test
		void testBuildingAllFields() throws Exception {
			final String attachmentFileName = "foobar.txt";
			final String attachmentContentType = "text/plain";
			final byte[] attachmentBytes = new byte[10];
			final SimpleSendEmailData underTest = SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT)
																		.cc(List.of(SAMPLE_CC))
																		.bcc(List.of(SAMPLE_BCC))
																		.bodyContentType(SAMPLE_CONTENT_TYPE.getContentTypeString())
																		.attachment(SendEmailData.toDataSource(attachmentFileName, attachmentBytes, attachmentContentType))
																		.build();
			
			assertAll(
					()->assertEquals(1, underTest.to().size()),
					()->assertEquals(SAMPLE_TO, underTest.to().get(0)),
					()->assertEquals(SAMPLE_FROM, underTest.from()),
					()->assertEquals(SAMPLE_SUBJECT, underTest.subject()),
					()->assertEquals(SAMPLE_BODY_CONTENT, underTest.bodyContent()),
					()->assertEquals(1, underTest.cc().size()),
					()->assertEquals(SAMPLE_CC, underTest.cc().get(0)),
					()->assertEquals(1, underTest.bcc().size()),
					()->assertEquals(SAMPLE_BCC, underTest.bcc().get(0)),
					()->assertEquals(SAMPLE_CONTENT_TYPE, underTest.bodyContentType().get()),
					()->assertEquals(1, underTest.attachments().size()),
					()->{
						DataSource attachments = underTest.attachments().get(0);
						assertAll(
								()->assertArrayEquals(attachmentBytes, attachments.getInputStream().readAllBytes()),
								()->assertSame(attachmentContentType, attachments.getContentType()),
								()->assertSame(attachmentFileName, attachments.getName())
								);
						
					}
					);
			
			// Make sure using another SendEmailData to initialize the builder works.
			final SimpleSendEmailData underTest2 = SimpleSendEmailData.getBuilder(underTest).build();
			
			assertAll(
					()->assertEquals(1, underTest2.to().size()),
					()->assertEquals(SAMPLE_TO, underTest2.to().get(0)),
					()->assertEquals(SAMPLE_FROM, underTest2.from()),
					()->assertEquals(SAMPLE_SUBJECT, underTest2.subject()),
					()->assertEquals(SAMPLE_BODY_CONTENT, underTest2.bodyContent()),
					()->assertEquals(1, underTest2.cc().size()),
					()->assertEquals(SAMPLE_CC, underTest2.cc().get(0)),
					()->assertEquals(1, underTest2.bcc().size()),
					()->assertEquals(SAMPLE_BCC, underTest2.bcc().get(0)),
					()->assertEquals(SAMPLE_CONTENT_TYPE, underTest2.bodyContentType().get()),
					()->assertEquals(1, underTest2.attachments().size()),
					()->{
						DataSource attachments = underTest2.attachments().get(0);
						assertAll(
								()->assertArrayEquals(attachmentBytes, attachments.getInputStream().readAllBytes()),
								()->assertSame(attachmentContentType, attachments.getContentType()),
								()->assertSame(attachmentFileName, attachments.getName())
								);
						
					}
					);

		}

		@Test
		void testBuildingNoOptionalFields() {
			final SimpleSendEmailData underTest = SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT)
																		.build();
			
			assertAll(
					()->assertEquals(1, underTest.to().size()),	// Make sure the mandatory fields are still correct.
					()->assertEquals(SAMPLE_TO, underTest.to().get(0)),
					()->assertEquals(SAMPLE_FROM, underTest.from()),
					()->assertEquals(SAMPLE_SUBJECT, underTest.subject()),
					()->assertEquals(SAMPLE_BODY_CONTENT, underTest.bodyContent()),
					()->assertEquals(0, underTest.cc().size()),	// Make sure the rest of the fields are empty.
					()->assertTrue(underTest.cc().isEmpty()),
					()->assertEquals(0, underTest.bcc().size()),
					()->assertTrue(underTest.bcc().isEmpty()),
					()->assertTrue(underTest.bodyContentType().isEmpty()),
					()->assertEquals(0, underTest.attachments().size()),
					()->assertTrue(underTest.attachments().isEmpty())
					);
		}

		@Test
		void testBuildingBadDataInFields() {
			final SimpleSendEmailData underTest = SimpleSendEmailData.getBuilder(List.of(SAMPLE_TO), SAMPLE_FROM, SAMPLE_SUBJECT, SAMPLE_BODY_CONTENT)
																		.bodyContentType("foo/bar")
																		.build();
			
			assertAll(
					()->assertEquals(1, underTest.to().size()),	// Make sure the mandatory fields are still correct.
					()->assertEquals(SAMPLE_TO, underTest.to().get(0)),
					()->assertEquals(SAMPLE_FROM, underTest.from()),
					()->assertEquals(SAMPLE_SUBJECT, underTest.subject()),
					()->assertEquals(SAMPLE_BODY_CONTENT, underTest.bodyContent()),
					()->assertEquals(0, underTest.cc().size()),	// Make sure the rest of the fields are empty.
					()->assertTrue(underTest.cc().isEmpty()),
					()->assertEquals(0, underTest.bcc().size()),
					()->assertTrue(underTest.bcc().isEmpty()),
					()->assertTrue(underTest.bodyContentType().isEmpty()),	// Make sure the bad body content is discarded. 
					()->assertEquals(0, underTest.attachments().size()),
					()->assertTrue(underTest.attachments().isEmpty())
					);
		}
	}
	
	/**
	 * Performs an assertThrows() and then asserts that the resulting exception's message contains one or more strings.
	 * 
	 * @param expectedType
	 * @param executable
	 * @param strings
	 * @return
	 * @throws MultipleFailuresError
	 */
	private static Executable assertThrowsAndMsgContains(final Class<? extends Exception> expectedType,
			final Executable executable, final String... strings) throws MultipleFailuresError {
		final Exception ex = assertThrows(expectedType, executable);
		final String msg = ex.getMessage();
		
		return ()->assertAll(Arrays.stream(strings).map((s)->(()->assertThat(msg, containsString(s)))));
	}

}
