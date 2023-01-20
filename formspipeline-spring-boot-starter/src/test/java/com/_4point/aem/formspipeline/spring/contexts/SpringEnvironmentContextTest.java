package com._4point.aem.formspipeline.spring.contexts;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionFailedException;

@SpringBootTest(classes=SpringEnvironmentContext.class)
// Choose properties that should existing on any developer or server machines and where values wouldn't 
// or shouldn't be different based on environment
class SpringEnvironmentContextTest {
	
    @Autowired
    private SpringEnvironmentContext underTest;

	@BeforeEach
	void setup() {
	}
	
	@Test
	void testGet_KeyNotFound() {		
		assertEquals(Optional.empty(), underTest.get("test", String.class));
	}

	@Test
	void testGet_WrongType() {
		ConversionFailedException ex = assertThrows(ConversionFailedException.class, ()->underTest.get("java.vendor.url", Integer.class));
		String msg = ex.getMessage();
		assertNotNull(msg);		
		assertThat(msg, allOf(containsString("Failed to convert from type [java.lang.String] to type [java.lang.Integer]")));		
	}

	@Test
	void testGet_nonStringProperty_returnNonStringValues() throws MalformedURLException {	
		assertEquals(false, underTest.get("spring.jmx.enabled", Boolean.class).get());		
		assertThat(underTest.get("java.vendor.url", URL.class).get(), is(instanceOf(URL.class)));
		//assertThat(underTest.get("os.version", Integer.class).get(), is(instanceOf(Integer.class)));
		assertThat(underTest.get("java.vm.specification.version", Integer.class).get(), is(instanceOf(Integer.class)));  
	}

	@Test
	void testGet_stringProperty_returnStringValue() {		
		//Number can always be string as well
		assertThat(underTest.get("java.specification.version", String.class).get(), is(instanceOf(String.class)));
//		assertEquals("UTF-8", underTest.get("file.encoding", String.class).get());
	}
}
