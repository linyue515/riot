package com.redislabs.riot.processor;

import com.redislabs.riot.convert.CollectionToStringMapConverter;
import com.redislabs.riot.convert.RegexNamedGroupsExtractor;
import com.redislabs.riot.convert.StreamToStringMapConverter;
import com.redislabs.riot.convert.StringToStringMapConverter;
import com.redislabs.riot.convert.ZsetToStringMapConverter;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.StreamMessage;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.redis.support.KeyValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeyValueMapItemProcessor implements ItemProcessor<KeyValue<String>, Map<String, Object>> {

	private final Converter<String, Map<String, String>> keyFieldsExtractor;
	private final Converter<Map<String, String>, Map<String, String>> hashConverter;
	private final Converter<List<StreamMessage<String, String>>, Map<String, String>> streamConverter;
	private final Converter<List<String>, Map<String, String>> listConverter;
	private final Converter<Set<String>, Map<String, String>> setConverter;
	private final Converter<List<ScoredValue<String>>, Map<String, String>> zsetConverter;
	private final Converter<String, Map<String, String>> stringConverter;
	private final Converter<Object, Map<String, String>> defaultConverter;

	public KeyValueMapItemProcessor(Converter<String, Map<String, String>> keyFieldsExtractor,
			Converter<Map<String, String>, Map<String, String>> hashConverter,
			Converter<List<String>, Map<String, String>> listConverter,
			Converter<Set<String>, Map<String, String>> setConverter,
			Converter<List<StreamMessage<String, String>>, Map<String, String>> streamConverter,
			Converter<String, Map<String, String>> stringConverter,
			Converter<List<ScoredValue<String>>, Map<String, String>> zsetConverter,
			Converter<Object, Map<String, String>> defaultConverter) {
		this.keyFieldsExtractor = keyFieldsExtractor;
		this.hashConverter = hashConverter;
		this.listConverter = listConverter;
		this.setConverter = setConverter;
		this.streamConverter = streamConverter;
		this.stringConverter = stringConverter;
		this.zsetConverter = zsetConverter;
		this.defaultConverter = defaultConverter;
	}

	@Override
	public Map<String, Object> process(KeyValue<String> item) {
		Map<String, Object> map = new HashMap<>(keyFieldsExtractor.convert(item.getKey()));
		Map<String, String> valueMap = map(item);
		if (valueMap != null) {
			map.putAll(valueMap);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> map(KeyValue<String> item) {
		switch (item.getType()) {
		case HASH:
			return hashConverter.convert((Map<String, String>) item.getValue());
		case LIST:
			return listConverter.convert((List<String>) item.getValue());
		case SET:
			return setConverter.convert((Set<String>) item.getValue());
		case ZSET:
			return zsetConverter.convert((List<ScoredValue<String>>) item.getValue());
		case STREAM:
			return streamConverter.convert((List<StreamMessage<String, String>>) item.getValue());
		case STRING:
			return stringConverter.convert((String) item.getValue());
		default:
			return defaultConverter.convert(item.getValue());
		}
	}

	public static KeyValueItemProcessorBuilder builder() {
		return new KeyValueItemProcessorBuilder();
	}

	@Accessors(fluent = true)
	@Setter
	public static class KeyValueItemProcessorBuilder {

		private String keyRegex;

		public KeyValueMapItemProcessor build() {
			Assert.notNull(keyRegex, "Key regex is required.");
			RegexNamedGroupsExtractor keyFieldsExtractor = RegexNamedGroupsExtractor.builder().regex(keyRegex).build();
			StreamToStringMapConverter streamConverter = StreamToStringMapConverter.builder().build();
			CollectionToStringMapConverter<List<String>> listConverter = CollectionToStringMapConverter
					.<List<String>>builder().build();
			CollectionToStringMapConverter<Set<String>> setConverter = CollectionToStringMapConverter
					.<Set<String>>builder().build();
			ZsetToStringMapConverter zsetConverter = ZsetToStringMapConverter.builder().build();
			Converter<String, Map<String, String>> stringConverter = StringToStringMapConverter.builder().build();
			return new KeyValueMapItemProcessor(keyFieldsExtractor, o -> o, listConverter, setConverter,
					streamConverter, stringConverter, zsetConverter, o -> null);
		}

	}
}
