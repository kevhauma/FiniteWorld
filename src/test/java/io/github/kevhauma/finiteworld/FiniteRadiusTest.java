package io.github.kevhauma.finiteworld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FiniteRadiusTest {
	@ParameterizedTest
	@ValueSource(strings = {"3000", "4000", " 5000 ", "20000"})
	void acceptsWholeNumbersInRange(String text) {
		assertEquals(OptionalInt.of(Integer.parseInt(text.strip())), FiniteRadius.parse(text));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "2999", "20001", "-4000", "4000.5", "4e3", "abc", "99999999999"})
	void rejectsAnythingElse(String text) {
		assertEquals(OptionalInt.empty(), FiniteRadius.parse(text));
	}

	@ParameterizedTest
	@ValueSource(floats = {3000.0F, 4000.0F, 20000.0F})
	void codecDecodesRadiusInRange(float radius) {
		assertEquals(radius, FiniteRadius.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(radius)).getOrThrow());
	}

	@ParameterizedTest
	@ValueSource(floats = {800.0F, 2999.0F, 20001.0F, -4000.0F})
	void codecRejectsRadiusOutOfRange(float radius) {
		assertTrue(FiniteRadius.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(radius)).isError());
	}
}
