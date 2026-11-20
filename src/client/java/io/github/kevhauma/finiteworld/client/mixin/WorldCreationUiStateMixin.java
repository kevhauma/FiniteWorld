package io.github.kevhauma.finiteworld.client.mixin;

import io.github.kevhauma.finiteworld.client.FiniteRadiusScreen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Gives the Finite preset a Customize button. Vanilla's {@code PresetEditor.EDITORS} is an immutable
 * {@code Map.of} on an interface, so the lookups that read it are wrapped instead of the map being extended.
 */
@Mixin(WorldCreationUiState.class)
abstract class WorldCreationUiStateMixin {
	@WrapOperation(method = {"getPresetEditor", "updatePresetLists"}, at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object finiteworld$finiteEditor(Map<Object, Object> editors, Object preset, Operation<Object> original) {
		return FiniteRadiusScreen.PRESET.equals(preset) ? FiniteRadiusScreen.EDITOR : original.call(editors, preset);
	}
}
