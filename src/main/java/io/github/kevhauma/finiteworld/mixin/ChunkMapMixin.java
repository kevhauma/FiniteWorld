package io.github.kevhauma.finiteworld.mixin;

import io.github.kevhauma.finiteworld.structure.AnchoredStructurePlacement;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** The structure state alone can't check whether vanilla starts a structure; hand the anchored fallback this world's generator and templates (STR-2). */
@Mixin(ChunkMap.class)
abstract class ChunkMapMixin {
	@Shadow
	@Final
	private ChunkGeneratorStructureState chunkGeneratorState;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void finiteworld$registerStructureContext(CallbackInfo ci, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) StructureTemplateManager templates,
		@Local(argsOnly = true) ChunkGenerator generator) {
		// A plain height accessor, not the level, so the registration holds no reference back to the state.
		AnchoredStructurePlacement.register(this.chunkGeneratorState, generator, level.registryAccess(), templates, LevelHeightAccessor.create(level.getMinY(), level.getHeight()));
	}
}
