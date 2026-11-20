package io.github.kevhauma.finiteworld;

import io.github.kevhauma.finiteworld.anchor.AnchorDensityFunction;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanFunction;
import io.github.kevhauma.finiteworld.edge.EdgeCapDensityFunction;
import io.github.kevhauma.finiteworld.edge.EdgeDensityFunction;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiniteWorld implements ModInitializer {
	public static final String MOD_ID = "finiteworld";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("edge"), EdgeDensityFunction.CODEC);
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("edge_cap"), EdgeCapDensityFunction.CODEC);
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("anchor"), AnchorDensityFunction.CODEC);
		Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("anchor_plan"), AnchorPlanFunction.CODEC);
		LOGGER.info("Finite World initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
