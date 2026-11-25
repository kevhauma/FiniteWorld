package io.github.kevhauma.finiteworld.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement;

/** The vanilla set's exclusion zone, which the anchored fallback honours (M8-04); vanilla keeps the getter protected. */
@Mixin(AbstractSpreadingStructurePlacement.class)
public interface SpreadingPlacementAccessor {
	@Invoker("exclusionZone")
	Optional<AbstractSpreadingStructurePlacement.ExclusionZone> finiteworld$exclusionZone();
}
