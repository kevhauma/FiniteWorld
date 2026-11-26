package io.github.kevhauma.finiteworld;

import io.github.kevhauma.finiteworld.anchor.AnchorPlanFunction;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.AnchorPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldDimensions;

/** UI-1: a chosen radius is saved in the world's generator settings and reaches every radius node, spawn search included. */
public class RadiusGameTest {
	private static final float RADIUS = 5000.0F;
	private static final Set<String> RADIUS_TYPES = Set.of("finiteworld:edge", "finiteworld:edge_cap", "finiteworld:anchor_plan");

	@GameTest
	public void radiusSavedInGeneratorSettings(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
		WorldDimensions preset = GuaranteeGameTest.finiteDimensions(registries);
		WorldDimensions custom = FiniteRadius.withRadius(preset, registries, RADIUS);

		// What world_gen_settings.dat stores: WorldGenSettings holds WorldDimensions.CODEC.
		JsonElement saved = WorldDimensions.CODEC.codec().encodeStart(ops, custom).getOrThrow();
		List<JsonObject> nodes = new ArrayList<>();
		collect(saved, nodes);
		Set<String> types = nodes.stream().map(node -> node.get("type").getAsString()).collect(Collectors.toSet());
		Set<Float> radii = nodes.stream().map(node -> node.get("radius").getAsFloat()).collect(Collectors.toSet());
		helper.assertTrue(types.equals(RADIUS_TYPES) && radii.equals(Set.of(RADIUS)),
			Component.literal("Saved radius nodes " + types + " with radii " + radii + ", expected " + RADIUS_TYPES + " at " + RADIUS));

		WorldDimensions reloaded = WorldDimensions.CODEC.codec().parse(ops, saved).getOrThrow();
		helper.assertTrue(FiniteRadius.of(reloaded).orElseThrow() == RADIUS && WorldDimensions.CODEC.codec().encodeStart(ops, reloaded).getOrThrow().equals(saved),
			Component.literal("Reloaded settings differ from the saved ones"));
		helper.succeed();
	}

	/** Spawn search runs first in a new world; it must plan at the world's radius, or the world keeps the registry's 4,000 plan. */
	@GameTest
	public void spawnSearchPlansAtWorldRadius(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		NoiseBasedChunkGenerator preset = (NoiseBasedChunkGenerator) GuaranteeGameTest.finiteOverworld(registries).generator();
		NoiseGeneratorSettings settings = FiniteRadius.withRadius(preset.generatorSettings().value(), RADIUS);
		NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(preset.getBiomeSource(), Holder.direct(settings));

		long start = AnchorPlanFunction.plansComputed();
		RandomState spawnFirst = RandomState.create(registries.lookupOrThrow(Registries.NOISE), 1L, settings);
		generator.getOrigin(spawnFirst);
		AnchorPlan plan = GuaranteeGameTest.planOf(spawnFirst, settings);
		long planned = AnchorPlanFunction.plansComputed() - start;
		AnchorPlan routerFirst = GuaranteeGameTest.planOf(RandomState.create(registries.lookupOrThrow(Registries.NOISE), 1L, settings), settings);

		helper.assertTrue(planned == 1 && plan.equals(routerFirst),
			Component.literal("Spawn search planned " + planned + " times; its plan " + (plan.equals(routerFirst) ? "matches" : "differs from") + " the router's"));
		helper.succeed();
	}

	private static void collect(JsonElement element, List<JsonObject> nodes) {
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("radius")) {
				nodes.add(object);
			}
			object.entrySet().forEach(entry -> collect(entry.getValue(), nodes));
		} else if (element.isJsonArray()) {
			element.getAsJsonArray().forEach(child -> collect(child, nodes));
		}
	}
}
