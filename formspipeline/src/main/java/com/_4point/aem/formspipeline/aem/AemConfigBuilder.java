package com._4point.aem.formspipeline.aem;

import java.util.Objects;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;

import com._4point.aem.docservices.rest_services.client.helpers.AemServerType;
import com._4point.aem.docservices.rest_services.client.helpers.Builder;

public abstract class AemConfigBuilder  {

	protected String machineName;
	protected Integer port;
	protected Boolean useSsl;
	protected Supplier<Client> clientFactory;
	protected String username;
	protected String password;
	protected Supplier<String> correlationIdFn;
	protected AemServerType serverType;
	
	public AemConfigBuilder machineName(String machineName) {
		this.machineName = machineName;
		return this;
	}

	public AemConfigBuilder port(Integer port) {
		this.port = port;
		return this;
	}

	public AemConfigBuilder useSsl(Boolean useSsl) {
		this.useSsl = useSsl;
		return this;
	}

	public AemConfigBuilder clientFactory(Supplier<Client> clientFactory) {
		this.clientFactory = clientFactory;
		return this;
	}

	public AemConfigBuilder basicAuthentication(String username, String password) {
		this.username = Objects.requireNonNull(username);
		this.password = Objects.requireNonNull(password);
		return this;
	}

	public AemConfigBuilder correlationId(Supplier<String> correlationIdFn) {
		this.correlationIdFn = correlationIdFn;
		return this;
	}

	public AemConfigBuilder aemServerType(AemServerType serverType) {
		this.serverType = serverType;
		return this;
	}

	protected <T extends Builder> T setBuilderFields(T builder) {
		if (machineName != null) { builder.machineName(machineName); }
		if (port != null) { builder.port(port); }
		if (useSsl != null) { builder.useSsl(useSsl); }
		if (clientFactory != null) { builder.clientFactory(clientFactory); }
		if (username != null && password != null) { builder.basicAuthentication(username, password); }
		if (correlationIdFn != null) { builder.correlationId(correlationIdFn); }
		if (serverType != null) { builder.aemServerType(serverType); }
		
		return builder;
	}
}
