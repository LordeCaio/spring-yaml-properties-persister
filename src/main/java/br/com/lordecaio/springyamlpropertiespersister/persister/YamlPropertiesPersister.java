package br.com.lordecaio.springyamlpropertiespersister.persister;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.lang.NonNull;
import org.springframework.util.PropertiesPersister;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class YamlPropertiesPersister implements PropertiesPersister {

	private final Charset encoding;
	private final DumperOptions options;

	YamlPropertiesPersister(Charset encoding, DumperOptions options) {
		this.encoding = encoding;
		this.options = options;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public DumperOptions getOptions() {
		return options;
	}

	@Override
	public void load(@NonNull Properties props, @NonNull InputStream is) {
		var yaml = new YamlPropertiesFactoryBean();
		yaml.setResources(new InputStreamResource(is));
		var loaded = yaml.getObject();
		if (loaded != null) {
			props.putAll(loaded);
		}
	}

	@Override
	public void load(@NonNull Properties props, @NonNull Reader reader) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			var builder = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line).append(System.lineSeparator());
			}
			byte[] bytes = builder.toString().getBytes(encoding);
			try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
				load(props, inputStream);
			}
		}
	}

	@Override
	public void store(@NonNull Properties props, @NonNull OutputStream os, @NonNull String header) throws IOException {
		try (Writer writer = new OutputStreamWriter(os, encoding)) {
			store(props, writer, header);
		}
	}

	@Override
	public void store(@NonNull Properties props, @NonNull Writer writer, @NonNull String header) throws IOException {
		writeHeader(writer, header);

		Map<String, Object> yamlMap = new LinkedHashMap<>();
		for (String key : props.stringPropertyNames()) {
			insertIntoMap(yamlMap, key, props.getProperty(key));
		}

		new Yaml(options).dump(yamlMap, writer);
	}

	private void writeHeader(@NonNull Writer writer, @NonNull String header) throws IOException {
		String headerMessage = header.startsWith("#") ? header : "#" + header;
		String lineBreak = options.getLineBreak().getString();

		writer.write(headerMessage + lineBreak);
	}

	@SuppressWarnings("unchecked")
	private void insertIntoMap(Map<String, Object> root, String key, String value) {
		String[] parts = key.split("\\.");
		Map<String, Object> current = root;
		for (int i = 0; i < parts.length - 1; i++) {
			current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new LinkedHashMap<>());
		}
		current.put(parts[parts.length - 1], value);
	}

	@Override
	public void loadFromXml(@NonNull Properties props, @NonNull InputStream is) throws IOException {
		props.loadFromXML(is);
	}

	@Override
	public void storeToXml(@NonNull Properties props, @NonNull OutputStream os, @NonNull String header)
		  throws IOException {
		props.storeToXML(os, header);
	}

	@Override
	public void storeToXml(@NonNull Properties props, @NonNull OutputStream os, @NonNull String header,
		  @NonNull String encoding) throws IOException {
		props.storeToXML(os, header, encoding);
	}
}
