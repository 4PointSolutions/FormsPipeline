package com._4point.aem.formspipeline.spring.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com._4point.aem.formspipeline.spring.utils.EmailService.SendEmailData.BodyContentType;

import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Generic service for sending Emails.  It relies on Spring's JavaMailSender to do this.
 *
 */
public class EmailService {
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	
	private final JavaMailSender javaMailSender;

	private EmailService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}
	
	public static EmailService from(JavaMailSender javaMailSender) {
		return new EmailService(javaMailSender);
	}

	public EmailService sendMail(SendEmailData emailData) throws EmailServiceException {

		final MimeMessage mailMessage = javaMailSender.createMimeMessage();

		try {
			final MimeMessageHelper msgHelper = new MimeMessageHelper(mailMessage, true);
			msgHelper.setTo(emailData.to().toArray(String[]::new));
			msgHelper.setCc(emailData.cc().toArray(String[]::new));
			msgHelper.setBcc(emailData.bcc().toArray(String[]::new));
			msgHelper.setSubject(emailData.subject());
			final BodyContentType bodyContentType = emailData.bodyContentType().orElse(BodyContentType.PLAIN_TEXT);
			msgHelper.setText(emailData.bodyContent(), bodyContentType.isHtml());
			msgHelper.setFrom(emailData.from());

			for (DataSource attachment : emailData.attachments()) {
				msgHelper.addAttachment(attachment.getName(), attachment);
			}
		} catch (MessagingException e) {
			throw new EmailServiceException("Error while creating email message.", e);
		}

		
		logger.info("Sending email to " + emailData.to().stream().collect(Collectors.joining(",")) + 
				((emailData.cc().size() > 0 ) ? (" , cc'ed to " + emailData.cc().stream().collect(Collectors.joining(","))) : "") + 
				((emailData.bcc().size() > 0 ) ? (" , bcc'ed to " + emailData.bcc().stream().collect(Collectors.joining(","))) : "") + 
				" with " +  emailData.attachments().size() + " attachments.");
		javaMailSender.send(mailMessage);
		return this;
	}

	public interface SendEmailData {

		public enum BodyContentType {
			PLAIN_TEXT("text/plain", false), HTML_TEXT("text/html", true);

			private final String contentTypeString;
			private final boolean html;

			private BodyContentType(String contentTypeString, boolean html) {
				this.contentTypeString = contentTypeString;
				this.html = html;
			}

			/**
			 * Indicates whether this content type is HTML or not (this is used by the
			 * MimeMessageHelper).
			 * 
			 * @return
			 */
			public boolean isHtml() {
				return html;
			}

			/**
			 * Returns the contentTypeString that this enum represents.
			 * 
			 * @return
			 */
			public String getContentTypeString() {
				return contentTypeString;
			}

			private static Set<BodyContentType> allTypes = EnumSet.allOf(BodyContentType.class);

			/**
			 * Convert a ContentBodyType string to an enum.
			 * 
			 * @param evaluate
			 * @return
			 */
			static Optional<BodyContentType> of(String evaluate) {
				if (evaluate != null) {
					for (BodyContentType type : allTypes) {
						if (type.getContentTypeString().equals(evaluate)) {
							return Optional.of(type);
						}
					}
				}
				return Optional.empty();
			}
		}

		List<String> to();
		List<String> cc();
		List<String> bcc();
		String from();
		String subject();
		String bodyContent();
		Optional<BodyContentType> bodyContentType();
		List<DataSource> attachments();

		public static List<String> convertCommaSeparated(String value) {
			return (value == null || value.isBlank()) ? Collections.emptyList()
					: stripAll(Arrays.asList(value.split(",", 100))); // Max 100 email addresses allowed.
		}

		private static List<String> stripAll(List<String> list) {
			list.replaceAll(String::strip); // strip whitespace from all the entries in the list.
			return list;
		}

		public static DataSource toDataSource(String dsName, byte[] dataBytes, String contentType) {
			Objects.requireNonNull(dataBytes);
			return new DataSource() {
				
				@Override
				public OutputStream getOutputStream() throws IOException {
					throw new UnsupportedOperationException("This is a read-only DataSource.");
				}
				
				@Override
				public String getName() {
					return dsName;
				}
				
				@Override
				public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream(dataBytes);
				}
				
				@Override
				public String getContentType() {
					return contentType;
				}
			};
		}

		private static String ValidateMandatory(String arg, String argName) {
			if (arg == null) {
				throw new IllegalArgumentException(argName + " cannot be null.");
			} else if (arg.isBlank()) {
				throw new IllegalArgumentException(argName + " cannot be blank.");
			} else {
				return arg;
			}
		}

		private static List<String> validateMandatory(List<String> arg, String argName) {
			if (arg == null) {
				throw new IllegalArgumentException(argName + " cannot be null.");
			}
			boolean needsSanitizing = false;
			for (String entry : arg) {
				if (entry == null || entry.isBlank()) { // Remove empty entries.
					needsSanitizing = true;
					break;
				}
			}
			if (needsSanitizing) {
				arg = sanitizeList(arg);
			}
			if (arg.isEmpty()) {
				throw new IllegalArgumentException(argName + " cannot be empty.");
			}
			return arg;
		}

		private static List<String> sanitizeList(List<String> values) {
			List<String> newList = new ArrayList<>(values.size());

			for (String entry : values) {
				if (entry != null && !entry.isBlank()) { // Remove empty entries.
					newList.add(entry.strip()); // strip any leading/trailing whitespace
				}
			}
			return newList;
		}

		private static List<String> normalizeOptional(List<String> arg) {
			if (arg == null) {
				return Collections.emptyList();
			}
			for (int i = 0; i < arg.size(); i++) {
				String entry = arg.get(i);
				if (entry == null || entry.isBlank()) { // Remove empty entries.
					arg.remove(i);
				}
			}
			return arg;
		}

		/**
		 * This class wraps a SendEmailData object and ensures that: a) Mandatory fields
		 * are present. b) Optional fields are not blank (converts blank fields into
		 * empty Optional<> elements)
		 *
		 * It is used to implement some frequently required code by classes that
		 * implement SendEmailData.
		 */
		public static class SafeSendEmailData implements SendEmailData {

			SendEmailData original;

			private SafeSendEmailData(SendEmailData original) {
				super();
				this.original = original;
			}

			@Override
			public List<String> to() {
				return validateMandatory(original.to(), "To");
			}

			@Override
			public List<String> cc() {
				return normalizeOptional(original.cc());
			}

			@Override
			public List<String> bcc() {
				return normalizeOptional(original.bcc());
			}

			@Override
			public String from() {
				return ValidateMandatory(original.from(), "From");
			}

			@Override
			public String subject() {
				return ValidateMandatory(original.subject(), "Subject");
			}

			@Override
			public String bodyContent() {
				return ValidateMandatory(original.bodyContent(), "BodyContent");
			}

			@Override
			public Optional<BodyContentType> bodyContentType() {
				return original.bodyContentType();
			}

			@Override
			public List<DataSource> attachments() {
				final List<DataSource> attachments = original.attachments();
				return attachments == null ? Collections.emptyList() : attachments;
			}

			public static SendEmailData from(SendEmailData original) {
				return new SafeSendEmailData(original);
			}
		}

	}

	@SuppressWarnings("serial")
	public static class EmailServiceException extends Exception {

		public EmailServiceException() {
			super();
		}

		public EmailServiceException(String message, Throwable cause) {
			super(message, cause);
		}

		public EmailServiceException(String message) {
			super(message);
		}

		public EmailServiceException(Throwable cause) {
			super(cause);
		}
	}

	static record SimpleSendEmailData(List<String> to, 
									  List<String> cc, 
									  List<String> bcc, 
									  String from, 
									  String subject, 
									  String bodyContent, 
									  Optional<BodyContentType> bodyContentType, 
									  List<DataSource> attachments
									  ) implements SendEmailData {

		private SimpleSendEmailData(List<String> to, List<String> cc, List<String> bcc,
				String from, String subject, String bodyContent, BodyContentType bodyContentType,
				List<DataSource> attachments) {
			this(SendEmailData.validateMandatory(to, "To"),
				 SendEmailData.normalizeOptional(cc),
				 SendEmailData.normalizeOptional(bcc),
				 SendEmailData.ValidateMandatory(from, "From"),
				 SendEmailData.ValidateMandatory(subject, "Subject"),
				 SendEmailData.ValidateMandatory(bodyContent, "BodyContent"),
				 Optional.ofNullable(bodyContentType),
				 List.copyOf(attachments)); // Make defensive copy.
		}

		public static SimpleSendEmailBuilder getBuilder(List<String> to, String from, String subject,
				String bodyContent) {
			return new SimpleSendEmailBuilder(to, from, subject, bodyContent);
		}

		public static SimpleSendEmailBuilder getBuilder(SendEmailData seedData) {
			final SimpleSendEmailBuilder simpleSendEmailBuilder = new SimpleSendEmailBuilder(seedData.to(),
					seedData.from(), seedData.subject(), seedData.bodyContent());
			simpleSendEmailBuilder.cc(seedData.cc());
			simpleSendEmailBuilder.bcc(seedData.bcc());
			seedData.bodyContentType().ifPresent(simpleSendEmailBuilder::bodyContentType);
			final List<DataSource> seedAttachments = seedData.attachments();
			if (seedAttachments.size() > 0) {
				simpleSendEmailBuilder.attachments(seedAttachments);
			}
			return simpleSendEmailBuilder;
		}

		public static class SimpleSendEmailBuilder {
			private List<String> to;
			private List<String> cc = null;
			private List<String> bcc = null;
			private String from;
			private String subject;
			private String bodyContent;
			private BodyContentType bodyContentType = null;
			private List<DataSource> attachments = new ArrayList<>();

			private SimpleSendEmailBuilder(List<String> to, String from, String subject, String bodyContent) {
				super();
				this.to = to;
				this.from = from;
				this.subject = subject;
				this.bodyContent = bodyContent;
			}

			public SimpleSendEmailBuilder cc(List<String> cc) {
				this.cc = cc;
				return this;
			}

			public SimpleSendEmailBuilder bcc(List<String> bcc) {
				this.bcc = bcc;
				return this;
			}

			public SimpleSendEmailBuilder bodyContentType(String bodyContentType) {
				this.bodyContentType = BodyContentType.of(bodyContentType).orElse(null);
				return this;
			}

			public SimpleSendEmailBuilder bodyContentType(BodyContentType bodyContentType) {
				this.bodyContentType = bodyContentType;
				return this;
			}

			public SimpleSendEmailBuilder attachments(List<DataSource> attachments) {
				this.attachments.addAll(attachments);
				return this;
			}

			public SimpleSendEmailBuilder attachment(DataSource attachment) {
				this.attachments.add(attachment);
				return this;
			}

			public SimpleSendEmailData build() {
				return new SimpleSendEmailData(to, cc, bcc, from, subject, bodyContent,
						bodyContentType, attachments);
			}
		}
	}
}
