package br.com.lordecaio.springyamlpropertiespersister.persister;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class YamlPropertiesPersisterTests {

	private static YamlPropertiesPersisterFactory factory;
	private static Properties props;

	@TempDir
	static Path tempDir;

	@BeforeAll
	static void setup() {
		factory = YamlPropertiesPersisterFactory.getFactory();
	}

	@BeforeEach
	void init() {
		props = new Properties();
	}

	static Stream<Arguments> providePersisterAndExtension() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		options.setIndent(4);

		return Stream.of(
			  Arguments.of(factory.createDefault(), ".yaml"),
			  Arguments.of(factory.createDefault(), ".yml"),
			  Arguments.of(factory.create(StandardCharsets.UTF_16, options), ".yaml"),
			  Arguments.of(factory.create(StandardCharsets.UTF_16, options), ".yml")
		);
	}

	private void populateSampleProperties(Properties props) {
		props.setProperty("key1", "This key was added");
		props.setProperty("nested.key1", "This nested key was added");
		props.setProperty("nested.key2", "This nested key was added too");
	}

	@ParameterizedTest
	@MethodSource("providePersisterAndExtension")
	void shouldLoadYamlFromInputStream(YamlPropertiesPersister persister, String fileExtension) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("messages" + fileExtension);
		assertNotNull(inputStream);

		persister.load(props, inputStream);

		assertAll(
			  () -> assertEquals("Hello, {0}! This is a YAML file", props.getProperty("greetings.hello-user")),
			  () -> assertEquals("Goodbye, {0}! See you later!", props.getProperty("greetings.goodbye-user"))
		);
	}

	@ParameterizedTest
	@MethodSource("providePersisterAndExtension")
	void shouldLoadYamlFromReader(YamlPropertiesPersister persister, String fileExtension) throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("messages" + fileExtension);
		assertNotNull(inputStream);

		try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			persister.load(props, reader);
		}

		assertAll(
			  () -> assertEquals("Hello, {0}! This is a YAML file", props.getProperty("greetings.hello-user")),
			  () -> assertEquals("Goodbye, {0}! See you later!", props.getProperty("greetings.goodbye-user"))
		);
	}

	@ParameterizedTest
	@MethodSource("providePersisterAndExtension")
	void shouldStoreAndReloadYamlUsingWriter(YamlPropertiesPersister persister, String fileExtension) throws IOException {
		populateSampleProperties(props);
		File temp = tempDir.resolve("generated-messages" + fileExtension).toFile();

		try (Writer writer = new FileWriter(temp, StandardCharsets.UTF_8)) {
			persister.store(props, writer, "auto-generated content");
		}

		try (InputStream inputStream = new FileInputStream(temp)) {
			persister.load(props, inputStream);
		}

		assertAll(
			  () -> assertEquals("This key was added", props.getProperty("key1")),
			  () -> assertEquals("This nested key was added", props.getProperty("nested.key1")),
			  () -> assertEquals("This nested key was added too", props.getProperty("nested.key2"))
		);
	}

	@ParameterizedTest
	@MethodSource("providePersisterAndExtension")
	void shouldStoreAndReloadYamlUsingOutputStream(YamlPropertiesPersister persister, String fileExtension) throws IOException {
		populateSampleProperties(props);
		File temp = tempDir.resolve("generated-messages" + fileExtension).toFile();

		try (OutputStream os = new FileOutputStream(temp)) {
			persister.store(props, os, "auto-generated content");
		}

		Properties reloaded = new Properties();
		try (InputStream inputStream = new FileInputStream(temp)) {
			persister.load(reloaded, inputStream);
		}

		assertAll(
			  () -> assertEquals("This key was added", reloaded.getProperty("key1")),
			  () -> assertEquals("This nested key was added", reloaded.getProperty("nested.key1")),
			  () -> assertEquals("This nested key was added too", reloaded.getProperty("nested.key2"))
		);
	}

	@Test
	void shouldLoadPropertiesFromValidXml() throws IOException {
		String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
            <properties>
              <entry key="key1">value1</entry>
            </properties>
            """;

		InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
		factory.createDefault().loadFromXml(props, inputStream);

		assertEquals("value1", props.getProperty("key1"));
	}

	@Test
	void shouldThrowIOExceptionForInvalidXml() {
		String invalidXml = "<properties><entry key=\"key1\">value1</entry>";
		InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes(StandardCharsets.UTF_8));

		assertThrows(IOException.class, () -> factory.createDefault().loadFromXml(props, inputStream));
	}

	@Test
	void shouldWritePropertiesToXml() throws IOException {
		props.setProperty("key1", "value1");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		factory.createDefault().storeToXml(props, outputStream, "header");

		String xmlOutput = outputStream.toString(StandardCharsets.UTF_8);
		assertTrue(xmlOutput.contains("<entry key=\"key1\">value1</entry>"));
	}

	@Test
	void shouldWritePropertiesToXmlWithEncoding() throws IOException {
		props.setProperty("key1", "value1");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		factory.createDefault().storeToXml(props, outputStream, "header", "UTF-8");

		String xmlOutput = outputStream.toString(StandardCharsets.UTF_8);
		assertTrue(xmlOutput.contains("<entry key=\"key1\">value1</entry>"));
	}

	@Test
	void shouldThrowIOExceptionForUnsupportedXmlEncoding() {
		props.setProperty("key1", "value1");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		assertThrows(IOException.class,
			  () -> factory.createDefault().storeToXml(props, outputStream, "header", "INVALID_ENCODING"));
	}
}
