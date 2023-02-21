package com._4point.aem.formspipeline.spring.destinations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Context.ContextBuilder;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.contexts.MapContext;
import com._4point.aem.formspipeline.results.SimpleResult;
import com._4point.aem.formspipeline.spring.utils.EmailService;
import com._4point.aem.formspipeline.spring.utils.EmailService.EmailServiceException;
import com._4point.aem.formspipeline.spring.utils.EmailService.SendEmailData;
import com._4point.aem.formspipeline.spring.utils.EmailService.SendEmailData.SafeSendEmailData;

import jakarta.activation.DataSource;

public class EmailDestination<DC extends Context, OC extends Context> implements OutputDestination<OutputChunk<DC, OC>, 
																								   Result<DC, OC, ? extends Context>> {

	private static final String EMAIL_DESTINATION_PREFIX = "com._4point.aem.formspipeline.spring.destinations.EmailDestination.";
	private static final String EMAIL_TO_KEY = EMAIL_DESTINATION_PREFIX + "to";
	private static final String EMAIL_CC_KEY = EMAIL_DESTINATION_PREFIX + "cc";
	private static final String EMAIL_BCC_KEY = EMAIL_DESTINATION_PREFIX + "bcc";
	private static final String EMAIL_FROM_KEY = EMAIL_DESTINATION_PREFIX + "from";
	private static final String EMAIL_SUBJECT_KEY = EMAIL_DESTINATION_PREFIX + "subject";
	private static final String EMAIL_BODY_KEY = EMAIL_DESTINATION_PREFIX + "body";
	
	private final EmailService emailService;
	
	public EmailDestination(EmailService emailService) {
		this.emailService = emailService;
	}

	@Override
	public Result<DC, OC, ? extends Context> process(OutputChunk<DC, OC> outputChunk) {
		// TODO: Attach PDF.
		
		Stream.of(outputChunk.dataContext())			// Get the data Context
			  .map(EmailDestination::reader)			// Create a ContextReader
			  .map(EmailDestination::toSendEmailData)	// Use that to create SendEmailData
			  .map(SafeSendEmailData::from)				// Make it a SafeSendEmailData
			  .forEach(this::sendEmail);				// Use it to send Email.

		return new SimpleResult<DC, OC, Context>(outputChunk.dataContext(), outputChunk.outputContext(), EmptyContext.emptyInstance());
	}

	private static SendEmailData toSendEmailData(ContextReader contextReader) {
		return new SendEmailData() {
			
			@Override
			public List<String> to() {
				return SendEmailData.convertCommaSeparated(contextReader.to());
			}
			
			@Override
			public String subject() {
				return contextReader.subject();
			}
			
			@Override
			public String from() {
				return contextReader.from();
			}
			
			@Override
			public List<String> cc() {
				return SendEmailData.convertCommaSeparated(contextReader.cc());
			}
			
			@Override
			public Optional<BodyContentType> bodyContentType() {
				return Optional.empty();
			}
			
			@Override
			public String bodyContent() {
				return contextReader.body();
			}
			
			@Override
			public List<String> bcc() {
				return SendEmailData.convertCommaSeparated(contextReader.bcc());
			}
			
			@Override
			public List<DataSource> attachments() {
				return List.of();
			}
		};
	}

	private void sendEmail(SendEmailData emailParameters) {
		try {
			emailService.sendMail(emailParameters );
		} catch (EmailServiceException e) {
			String msg = e.getMessage();
			throw new IllegalStateException("Error while sending email. (%s)".formatted(msg != null ? msg : e.getClass().getName()), e);
		}
	}

	public static ContextReader reader(Context context) { return new ContextReader(context); }
	
	public static ContextWriter writer() { return new ContextWriter(); }
	
	public static class ContextReader {
		private final Context context;
		
		private ContextReader(Context context) {
			this.context = context;
		}

		public String to() { return getMandatory(EMAIL_TO_KEY, "to"); } 
		public String cc() { return getOptional(EMAIL_CC_KEY); } 
		public String bcc() { return getOptional(EMAIL_BCC_KEY); } 
		public String from() { return getMandatory(EMAIL_FROM_KEY, "from"); } 
		public String subject() { return getMandatory(EMAIL_SUBJECT_KEY, "subject"); } 
		public String body() { return getMandatory(EMAIL_BODY_KEY, "body"); } 
		
		private String getMandatory(String key, String fieldName) {
			return context.getString(key).orElseThrow(()->new EmailDestinationException("'" + fieldName + "' field not supplied, it is mandatory."));
		}
		
		private String getOptional(String key) {
			return context.getString(key).orElse("");
		}
	}
	
	public static class ContextWriter {
		private final ContextBuilder builder;
		
		private ContextWriter() 											{ this(MapContext.builder());}
		private ContextWriter(ContextBuilder builder) 						{ this.builder = builder;}

		public ContextWriter to(String to) { this.builder.put(EMAIL_TO_KEY, to); return this;  } 
		public ContextWriter cc(String cc) { this.builder.put(EMAIL_CC_KEY, cc); return this;  } 
		public ContextWriter bcc(String bcc) { this.builder.put(EMAIL_BCC_KEY, bcc); return this;  } 
		public ContextWriter from(String from) { this.builder.put(EMAIL_FROM_KEY, from); return this;  } 
		public ContextWriter subject(String subject) { this.builder.put(EMAIL_SUBJECT_KEY, subject); return this;  } 
		public ContextWriter body(String body) { this.builder.put(EMAIL_BODY_KEY, body); return this;  } 
		
		// Convenience Functions
		public ContextWriter to(List<String> to) { return to(toCommaSeparatedString(to)); } 
		public ContextWriter cc(List<String> cc) { return cc(toCommaSeparatedString(cc)); } 
		public ContextWriter bcc(List<String> bcc) { return bcc(toCommaSeparatedString(bcc)); } 

		private String toCommaSeparatedString(List<String> list) { return list.stream().collect(Collectors.joining(",")); } 
		
		public Context build() 	{ return builder.build(); }
	}
	
	@SuppressWarnings("serial")
	public static class EmailDestinationException extends IllegalArgumentException {

		public EmailDestinationException() {
			super();
		}

		public EmailDestinationException(String message, Throwable cause) {
			super(message, cause);
		}

		public EmailDestinationException(String s) {
			super(s);
		}

		public EmailDestinationException(Throwable cause) {
			super(cause);
		}
	}
}
