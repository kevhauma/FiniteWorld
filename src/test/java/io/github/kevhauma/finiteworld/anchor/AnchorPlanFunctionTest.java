package io.github.kevhauma.finiteworld.anchor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AnchorPlanFunctionTest {
	@BeforeAll
	static void bootstrap() {
		// Loading the density function classes reads vanilla registries.
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void bandMustLeaveRoomForAnAnchor() {
		assertTrue(AnchorPlanFunction.validate(plan(3000.0F, 500.0F)).isSuccess());
		assertTrue(AnchorPlanFunction.validate(plan(3000.0F, 3000.0F - AnchorPlanner.ANCHOR_RADIUS)).isSuccess());
		assertTrue(AnchorPlanFunction.validate(plan(3000.0F, 3000.0F - AnchorPlanner.ANCHOR_RADIUS + 1)).isError());
		assertTrue(AnchorPlanFunction.validate(plan(3000.0F, 5000.0F)).isError());
	}

	// The checks read radius and band only; decoding with the real biome list is covered by AnchorPlanGameTest.
	private static AnchorPlanFunction plan(float radius, float band) {
		return new AnchorPlanFunction(radius, band, null, null, null, null, null, null, null);
	}
}
