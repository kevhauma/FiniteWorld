package io.github.kevhauma.finiteworld;

import io.github.kevhauma.finiteworld.anchor.AnchorDensityFunction;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanFunction;
import io.github.kevhauma.finiteworld.edge.EdgeCapDensityFunction;
import io.github.kevhauma.finiteworld.edge.EdgeDensityFunction;
import io.github.kevhauma.finiteworld.structure.AnchoredStructurePlacement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class FiniteWorld implements ModInitializer {
	public static final String MOD_ID = "finiteworld";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("edge"), EdgeDensityFunction.CODEC);
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("edge_cap"), EdgeCapDensityFunction.CODEC);
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("anchor"), AnchorDensityFunction.CODEC);
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("anchor_plan"), AnchorPlanFunction.CODEC);
		Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, id("anchored"), AnchoredStructurePlacement.CODEC);
		LOGGER.info("Finite World initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
