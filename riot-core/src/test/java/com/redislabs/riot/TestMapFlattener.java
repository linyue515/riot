package com.redislabs.riot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.vault.support.JsonMapFlattener;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestMapFlattener {

	@Test
	public void testNestedKeys() {
		Map<String, Object> map = map("1", map("1", "1.1", "2", "1.2"), "2", map("1", "2.1", "2", "2.2"));
		Map<String, String> expected = new HashMap<>();
		expected.putAll((Map) map("1.1", "1.1", "1.2", "1.2"));
		expected.putAll((Map) map("2.1", "2.1", "2.2", "2.2"));
		Assertions.assertEquals(expected, JsonMapFlattener.flatten(map));
	}

	@Test
	public void testIndexedKeys() {
		Map<String, Object> map = map("1", Arrays.asList("1.1", "1.2"), "2", Arrays.asList("2.1", "2.2"));
		Map<String, String> expected = new HashMap<>();
		expected.putAll((Map) map("1[0]", "1.1", "1[1]", "1.2"));
		expected.putAll((Map) map("2[0]", "2.1", "2[1]", "2.2"));
		Assertions.assertEquals(expected, JsonMapFlattener.flatten(map));
	}

	@Test
	public void testMixedValues() {
		Map<String, Object> map = map("1", Arrays.asList("1.1", "1.2"), "2", map("1", "2.1", "2", "2.2"));
		Map<String, String> expected = new HashMap<>();
		expected.putAll((Map) map("1[0]", "1.1", "1[1]", "1.2"));
		expected.putAll((Map) map("2.1", "2.1", "2.2", "2.2"));
		Assertions.assertEquals(expected, JsonMapFlattener.flatten(map));
	}

	private Map<String, Object> map(String field1, Object value1, String field2, Object value2) {
		Map<String, Object> map = new HashMap<>();
		map.put(field1, value1);
		map.put(field2, value2);
		return map;
	}
}
