package io.github.kevhauma.finiteworld.mixin;

import io.github.kevhauma.finiteworld.structure.AnchoredStructurePlacement;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;

/** An anchored placement's single position comes from the anchor plan, computed once per world where vanilla computes ring positions (STR-2). */
@Mixin(ChunkGeneratorStructureState.class)
abstract class ChunkGeneratorStructureStateMixin {
	@Inject(method = "generateRingPositions", at = @At("HEAD"), cancellable = true)
	private void finiteworld$anchoredPosition(Holder<StructureSet> set, ConcentricRingsStructurePlacement placement,
		CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> cir) {
		if (placement instanceof AnchoredStructurePlacement anchored) {
			cir.setReturnValue(CompletableFuture.supplyAsync(() -> anchored.positions((ChunkGeneratorStructureState) (Object) this),
				Util.backgroundExecutor().forName("finiteworldFallback")));
		}
	}
}
