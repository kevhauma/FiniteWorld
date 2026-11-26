package io.github.kevhauma.finiteworld.mixin;

import io.github.kevhauma.finiteworld.FiniteRadius;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.SpawnTargetPoint;

/** The spawn search samples the world's own radius nodes, not the registry's (see {@link FiniteRadius#withRadius(List, float)}). */
@Mixin(NoiseBasedChunkGenerator.class)
abstract class NoiseBasedChunkGeneratorMixin {
	@ModifyExpressionValue(method = "getOrigin",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;spawnTarget()Ljava/util/List;"))
	private List<SpawnTargetPoint> finiteworld$spawnTargetAtWorldRadius(List<SpawnTargetPoint> original) {
		return FiniteRadius.of(((NoiseBasedChunkGenerator) (Object) this).generatorSettings().value())
			.map(radius -> FiniteRadius.withRadius(original, radius))
			.orElse(original);
	}
}
