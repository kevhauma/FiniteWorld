package io.github.kevhauma.finiteworld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalInt;
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
}
