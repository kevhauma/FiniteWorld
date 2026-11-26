package io.github.kevhauma.finiteworld.mixin;

import io.github.kevhauma.finiteworld.FiniteRadius;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.levelgen.WorldDimensions;

/** Dedicated servers have no Create World screen: the radius comes from {@code generator-settings} in server.properties. */
@Mixin(targets = "net.minecraft.server.dedicated.DedicatedServerProperties$WorldDimensionData")
abstract class WorldDimensionDataMixin {
	@Shadow
	@Final
	private JsonObject generatorSettings;

	@ModifyReturnValue(method = "create", at = @At("RETURN"))
	private WorldDimensions finiteworld$radiusFromGeneratorSettings(WorldDimensions dimensions, HolderLookup.Provider registries) {
		return FiniteRadius.withConfiguredRadius(dimensions, registries, this.generatorSettings);
	}
}
