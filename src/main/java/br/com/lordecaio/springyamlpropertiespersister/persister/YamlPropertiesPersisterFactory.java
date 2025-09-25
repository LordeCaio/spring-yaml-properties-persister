package br.com.lordecaio.springyamlpropertiespersister.persister;

import org.yaml.snakeyaml.DumperOptions;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class YamlPropertiesPersisterFactory {

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final DumperOptions DEFAULT_DUMPER_OPTIONS = getDefaultDumperOptions();

	private YamlPropertiesPersisterFactory() { }

	public static YamlPropertiesPersisterFactory getFactory() {
		return new YamlPropertiesPersisterFactory();
	}

	public YamlPropertiesPersister create(Charset encoding, DumperOptions options) {
		var finalEncoding = Optional.ofNullable(encoding).orElse(DEFAULT_CHARSET);
		var finalOptions = Optional.ofNullable(options).orElse(DEFAULT_DUMPER_OPTIONS);
		return new YamlPropertiesPersister(finalEncoding, finalOptions);
	}

	public YamlPropertiesPersister createDefault() {
		return create(null, null);
	}

	private static DumperOptions getDefaultDumperOptions() {
		var options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		options.setIndent(2);
		return options;
	}
}
