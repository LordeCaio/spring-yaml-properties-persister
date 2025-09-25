package br.com.lordecaio.springyamlpropertiespersister;

import br.com.lordecaio.springyamlpropertiespersister.persister.YamlPropertiesPersisterFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.PropertiesPersister;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class SpringYamlPropertiesPersisterApplicationTests {

	private static final String TEMPORARY_FILE_NAME = "temp-messages";
	private static final String TEMPORARY_FILE_EXTENSION = ".yaml";

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private File temporaryMessageFile;

	@Autowired
	private PropertiesPersister propertiesPersister;

	@Test
	void shouldGetSuccessMessage() {
		assertNotNull(messageSource);
		var success = messageSource.getMessage("success", null, null);

		assertNotNull(success);
		assertEquals("Successfully loaded messages from YAML file!", success);
	}

	@Test
	void shouldGetAnotherSuccessMessage() {
		assertNotNull(messageSource);
		var anotherSuccess = messageSource.getMessage("another-success", null, null);

		assertNotNull(anotherSuccess);
		assertEquals("Another successful message from YAML!", anotherSuccess);
	}

	@Test
	void shouldAddNewPropertyAndRetrieveIt() throws Exception {
		assertNotNull(propertiesPersister);

		var newKey = "new-success";
		var newValue = "A newly added successful message from YAML!";

		var props = new Properties();
		try (var reader = new FileReader(temporaryMessageFile)) {
			propertiesPersister.load(props, reader);
		}

		try (var writer = new FileWriter(temporaryMessageFile)) {
			props.setProperty(newKey, newValue);
			propertiesPersister.store(props, writer, "Adding new message for testing");
		}

		// Wait for cache to expire
		Thread.sleep(1000);

		var retrievedMessage = messageSource.getMessage(newKey, null, null);
		assertNotNull(retrievedMessage);
		assertEquals(newValue, retrievedMessage);
	}

	@Configuration
	static class MessageConfiguration {

		@Bean
		File temporaryMessageFile() throws Exception {
			var tempFile = File.createTempFile(TEMPORARY_FILE_NAME, TEMPORARY_FILE_EXTENSION);
			tempFile.deleteOnExit();
			return tempFile;
		}

		@Bean
		@DependsOn("temporaryMessageFile")
		PropertiesPersister propertiesPersister(File temporaryMessageFile) {
			var persister = YamlPropertiesPersisterFactory.getFactory().createDefault();
			var properties = new Properties();

			properties.setProperty("success", "Successfully loaded messages from YAML file!");
			properties.setProperty("another-success", "Another successful message from YAML!");

			try (var writer = new FileWriter(temporaryMessageFile)) {
				persister.store(properties, writer, "Temporary messages for testing");
			} catch (Exception e) {
				throw new RuntimeException("Failed to create temporary message file", e);
			}

			return persister;
		}

		@Bean
		@DependsOn({"temporaryMessageFile", "propertiesPersister"})
		MessageSource messageSource(File temporaryMessageFile, PropertiesPersister propertiesPersister) {
			ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
			messageSource.setFileExtensions(List.of(".yaml", ".yml"));
			messageSource.setBasename(
				  "file:" + temporaryMessageFile.getAbsolutePath().replace(TEMPORARY_FILE_EXTENSION, ""));
			messageSource.setPropertiesPersister(propertiesPersister);
			messageSource.setDefaultEncoding("UTF-8");
			messageSource.setCacheMillis(500);
			return messageSource;
		}
	}
}
