package com._4point.aem.formspipeline.spring.contexts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

@SpringBootApplication
@ExtendWith(MockitoExtension.class)
class SpringEnvironmentContextTest {
	
	@Mock Environment mockSpringEnv = new MockEnvironment();
	
    @Autowired
    private SpringEnvironmentContext underTest = new SpringEnvironmentContext();

	@BeforeEach
	void setup() {
	}

	@Test
	void testGet_stringProperty_returnNonStringValues() throws MalformedURLException {	
		Mockito.when(mockSpringEnv.getProperty("PID", Integer.class)).thenReturn(14424);
		Mockito.when(mockSpringEnv.getProperty("spring.jmx.enabled", Boolean.class)).thenReturn(false);
		Mockito.when(mockSpringEnv.getProperty("java.vendor.url", URL.class)).thenReturn(new URL("https://www.4point.com/content/4point/us/en/home.html"));
		underTest.setSpringEnvironment(mockSpringEnv);
		
		assertEquals(14424, underTest.get("PID", Integer.class).get());
		assertEquals(false, underTest.get("spring.jmx.enabled", Boolean.class).get());
		assertEquals(new URL("https://www.4point.com/content/4point/us/en/home.html"), underTest.get("java.vendor.url", URL.class).get());
	}
	
	@Test
	void testGet_KeyNotFound() {		
		underTest.setSpringEnvironment(mockSpringEnv);
		assertEquals(Optional.empty(), underTest.get("test", String.class));
	}

	@Test
	void testGet_WrongType() {
		lenient().when(mockSpringEnv.getProperty("os.name", String.class)).thenReturn("Windows 11");		
		underTest.setSpringEnvironment(mockSpringEnv);
		assertEquals(Optional.empty(), underTest.get("os.name", Integer.class));		
	}
	
	@Test
	void testGet_stringProperty_returnStringValue() {		
		Mockito.when(mockSpringEnv.getProperty("os.name", String.class)).thenReturn("Windows 11");
		Mockito.when(mockSpringEnv.getProperty("java.specification.version", String.class)).thenReturn("17.0.4.1+1");
		Mockito.when(mockSpringEnv.getProperty("PROCESSOR_IDENTIFIER", String.class)).thenReturn("Intel64 Family 6 Model 142 Stepping 12, GenuineIntel");
		Mockito.when(mockSpringEnv.getProperty("ProgramFiles(x86)", String.class)).thenReturn("C:\\Program Files (x86)");
		Mockito.when(mockSpringEnv.getProperty("file.separator", String.class)).thenReturn("\\");		
		underTest.setSpringEnvironment(mockSpringEnv);
		
		assertEquals("Windows 11", underTest.get("os.name", String.class).get());
		assertEquals("17.0.4.1+1", underTest.get("java.specification.version", String.class).get());
		assertEquals("Intel64 Family 6 Model 142 Stepping 12, GenuineIntel", underTest.get("PROCESSOR_IDENTIFIER", String.class).get());
		assertEquals("C:\\Program Files (x86)", underTest.get("ProgramFiles(x86)", String.class).get());
		assertEquals("\\", underTest.get("file.separator", String.class).get());
	}


}
