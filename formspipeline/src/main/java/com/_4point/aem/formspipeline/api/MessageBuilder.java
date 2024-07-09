package com._4point.aem.formspipeline.api;

public class MessageBuilder<T> {
//	private T payload;
//	private Context context;
	
//
//	static <T> MessageBuilder<T> fromMessage(Message<T> message) {
//		
//	}
	
//	static <T> MessageBuilder<T> withPayload(T payload) {
//		
//	}
	
	public static <T> Message<T> fromMessageReplacingPayload(Message<?> message, T payload, Context... additionalContexts) {
		return createMessage(payload, message.context().incorporate(additionalContexts));
	}

	public static <T> Message<T> fromMessage(Message<T> message, Context...  additionalContexts) {
		return createMessage(message.payload(), message.context().incorporate(additionalContexts));
	}

	/**
	 * A shortcut factory method for creating a message with the given payload
	 * and {@code Context}.
	 * <p><strong>Note:</strong> the given {@code MessageHeaders} instance is used
	 * directly in the new message, i.e. it is not copied.
	 * @param payload the payload to use (never {@code null})
	 * @param context the context to use (never {@code null})
	 * @return the created message
	 */
	public static <T> Message<T> createMessage(T payload, Context context) {
		return new MessageImpl<T>(payload, context);
	}
	
	private record MessageImpl<T>(T payload, Context context) implements Message<T> {};
}