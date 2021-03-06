package com.redislabs.riot.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.redis.support.KeyValue;

import io.lettuce.core.ScoredValue;
import io.lettuce.core.StreamMessage;

public class JsonKeyValueItemProcessor implements ItemProcessor<KeyValue<String>, KeyValue<String>> {

	@SuppressWarnings({ "unchecked", "incomplete-switch" })
	@Override
	public KeyValue<String> process(KeyValue<String> item) throws Exception {
		switch (item.getType()) {
		case ZSET:
			Collection<Map<String, Object>> zset = (Collection<Map<String, Object>>) item.getValue();
			Collection<ScoredValue<String>> values = new ArrayList<>(zset.size());
			for (Map<String, Object> map : zset) {
				double score = ((Number) map.get("score")).doubleValue();
				String value = (String) map.get("value");
				values.add(ScoredValue.fromNullable(score, value));
			}
			item.setValue(values);
			break;
		case STREAM:
			Collection<Map<String, Object>> stream = (Collection<Map<String, Object>>) item.getValue();
			Collection<StreamMessage<String, String>> messages = new ArrayList<>(stream.size());
			for (Map<String, Object> message : stream) {
				messages.add(new StreamMessage<>((String) message.get("stream"), (String) message.get("id"),
						(Map<String, String>) message.get("body")));
			}
			item.setValue(messages);
			break;
		}
		return item;
	}

}
