package com._4point.aem.formspipeline.spring.contexts;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes=SpringEnvironmentContext.class)
class SpringEnvironmentContextTest {
	
    @Autowired
    private SpringEnvironmentContext underTest;

	@BeforeEach
	void setup() {
	}

	@Test
	void testGet_stringProperty_returnNonStringValues() throws MalformedURLException {	
		assertNotNull(underTest);
		
		assertEquals(false, underTest.get("spring.jmx.enabled", Boolean.class).get());
//		assertEquals(new URL("https://www.4point.com/content/4point/us/en/home.html"), underTest.get("java.vendor.url", URL.class).get());
	}
	
	@Test
	void testGet_KeyNotFound() {		
		assertEquals(Optional.empty(), underTest.get("test", String.class));
	}

	@Disabled("Doesn't work yet, still work in progress.")
	@Test
	void testGet_WrongType() {
		assertEquals(Optional.empty(), underTest.get("os.name", Integer.class));		
	}
	
	@Disabled("Doesn't work yet, still work in progress.")
	@Test
	void testGet_stringProperty_returnStringValue() {		
		assertEquals("Windows 11", underTest.get("os.name", String.class).get());
		assertEquals("17.0.4.1+1", underTest.get("java.specification.version", String.class).get());
		assertEquals("Intel64 Family 6 Model 142 Stepping 12, GenuineIntel", underTest.get("PROCESSOR_IDENTIFIER", String.class).get());
		assertEquals("C:\\Program Files (x86)", underTest.get("ProgramFiles(x86)", String.class).get());
		assertEquals("\\", underTest.get("file.separator", String.class).get());
	}
}
