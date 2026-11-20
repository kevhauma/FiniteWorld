package io.github.kevhauma.finiteworld;

import io.github.kevhauma.finiteworld.anchor.AnchorPlanFunction;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.AnchorPlan;

import java.util.stream.Stream;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** ANC-1: the anchor plan is computed once per Finite world (one RandomState), never for Default, and only from the seed. */
public class AnchorPlanGameTest {
	private static final ResourceKey<DensityFunction> PLAN = ResourceKey.create(Registries.DENSITY_FUNCTION, FiniteWorld.id("overworld/anchor_plan"));
	private static final long SEED = 12345L;

	@GameTest
	public void planOncePerWorldNeverForDefault(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		HolderGetter<NormalNoise> noises = registries.lookupOrThrow(Registries.NOISE);
		NoiseGeneratorSettings finite = ((NoiseBasedChunkGenerator) GuaranteeGameTest.finiteOverworld(registries).generator()).generatorSettings().value();
		NoiseGeneratorSettings vanilla = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getValueOrThrow(NoiseGeneratorSettings.OVERWORLD);
		DensityFunction planFunction = registries.lookupOrThrow(Registries.DENSITY_FUNCTION).getValueOrThrow(PLAN);

		long start = AnchorPlanFunction.plansComputed();
		RandomState world = compileRouter(noises, SEED, finite);
		AnchorPlan plan = planOf(world, planFunction);
		helper.assertTrue(AnchorPlanFunction.plansComputed() - start == 1,
			Component.literal("Compiling the Finite router planned " + (AnchorPlanFunction.plansComputed() - start) + " times, expected once"));

		AnchorPlan reloaded = planOf(compileRouter(noises, SEED, finite), planFunction);
		helper.assertTrue(reloaded != plan && reloaded.equals(plan), Component.literal("Same seed gave a different plan: " + plan + " vs " + reloaded));
		helper.assertTrue(!planOf(compileRouter(noises, SEED + 1, finite), planFunction).equals(plan), Component.literal("Plan does not depend on the seed"));

		long beforeDefault = AnchorPlanFunction.plansComputed();
		compileRouter(noises, SEED, vanilla);
		helper.assertTrue(AnchorPlanFunction.plansComputed() == beforeDefault, Component.literal("A Default world planned anchors"));

		FiniteWorld.LOGGER.info("[ANC-1] seed {}: plan hash {}, anchors {}", SEED, plan.hashCode(), plan.anchors());
		helper.succeed();
	}

	/** A fresh RandomState with every router function compiled, as world generation does. */
	private static RandomState compileRouter(HolderGetter<NormalNoise> noises, long seed, NoiseGeneratorSettings settings) {
		RandomState random = RandomState.create(noises, seed, settings);
		NoiseRouter r = settings.noiseRouter();
		Stream.of(r.temperature(), r.vegetation(), r.continents(), r.erosion(), r.depth(), r.ridges(), r.chunkSurfaceLevel(), r.finalDensity())
			.forEach(random::getSampler);
		return random;
	}

	private static AnchorPlan planOf(RandomState random, DensityFunction planFunction) {
		return ((AnchorPlanFunction.Sampler) random.getSampler(planFunction)).plan();
	}
}
