package br.com.lordecaio.springyamlpropertiespersister.persister;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YamlPropertiesPersisterFactoryTests {

	private static YamlPropertiesPersisterFactory factory;

	@BeforeAll
	static void setup() {
		factory = YamlPropertiesPersisterFactory.getFactory();
	}

	@Test
	@DisplayName("Should create default YamlPropertiesPersister")
	void shouldCreateDefaultYamlPropertiesPersister() {
		YamlPropertiesPersister persister = factory.createDefault();
		assertNotNull(persister);
		assertEquals(YamlPropertiesPersisterFactory.DEFAULT_CHARSET, persister.getEncoding());
		assertEquals(YamlPropertiesPersisterFactory.DEFAULT_DUMPER_OPTIONS, persister.getOptions());
	}

	@Test
	@DisplayName("Should create default YamlPropertiesPersister when null arguments are provided")
	void shouldCreateDefaultYamlPropertiesPersisterWithNullArgs() {
		YamlPropertiesPersister persister = factory.create(null, null);
		assertNotNull(persister);
		assertEquals(YamlPropertiesPersisterFactory.DEFAULT_CHARSET, persister.getEncoding());
		assertEquals(YamlPropertiesPersisterFactory.DEFAULT_DUMPER_OPTIONS, persister.getOptions());
	}

	@Test
	@DisplayName("Should create YamlPropertiesPersister with custom encoding and options")
	void shouldCreateYamlPropertiesPersisterWithCustomEncoding() {
		var expectedEncoding = StandardCharsets.ISO_8859_1;
		YamlPropertiesPersister persister = factory.create(expectedEncoding, null);
		assertNotNull(persister);
		assertEquals(expectedEncoding, persister.getEncoding());
		assertEquals(YamlPropertiesPersisterFactory.DEFAULT_DUMPER_OPTIONS, persister.getOptions());
	}

	@Test
	@DisplayName("Should create YamlPropertiesPersister with custom options")
	void shouldCreateYamlPropertiesPersisterWithCustomOptions() {
		var expectedOptions = new org.yaml.snakeyaml.DumperOptions();
		expectedOptions.setDefaultFlowStyle(org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW);
		YamlPropertiesPersister persister = factory.create(null, expectedOptions);
		assertNotNull(persister);
		assertEquals(YamlPropertiesPersisterFactory.DEFAULT_CHARSET, persister.getEncoding());
		assertEquals(expectedOptions, persister.getOptions());
	}

	@Test
	@DisplayName("Should create YamlPropertiesPersister with custom encoding and options")
	void shouldCreateYamlPropertiesPersisterWithCustomEncodingAndOptions() {
		var expectedEncoding = StandardCharsets.ISO_8859_1;
		var expectedOptions = new org.yaml.snakeyaml.DumperOptions();
		expectedOptions.setDefaultFlowStyle(org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW);
		YamlPropertiesPersister persister = factory.create(expectedEncoding, expectedOptions);
		assertNotNull(persister);
		assertEquals(expectedEncoding, persister.getEncoding());
		assertEquals(expectedOptions, persister.getOptions());
	}
}
