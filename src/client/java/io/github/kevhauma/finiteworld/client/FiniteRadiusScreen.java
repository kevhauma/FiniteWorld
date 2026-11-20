package io.github.kevhauma.finiteworld.client;

import io.github.kevhauma.finiteworld.FiniteRadius;
import io.github.kevhauma.finiteworld.FiniteWorld;

import java.util.Optional;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/** The Finite preset's Customize screen: one radius field, Done only for a whole number in [MIN, MAX]. */
public class FiniteRadiusScreen extends Screen {
	public static final Optional<ResourceKey<WorldPreset>> PRESET = Optional.of(ResourceKey.create(Registries.WORLD_PRESET, FiniteWorld.id("finite")));
	public static final PresetEditor EDITOR = (parent, settings) -> new FiniteRadiusScreen(parent,
		FiniteRadius.of(settings.selectedDimensions()).orElseThrow().intValue(),
		radius -> parent.getUiState().updateDimensions((registries, dimensions) -> FiniteRadius.withRadius(dimensions, registries, radius)));

	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen parent;
	private final int radius;
	private final IntConsumer apply;

	private FiniteRadiusScreen(Screen parent, int radius, IntConsumer apply) {
		super(Component.translatable("createWorld.customize.finiteworld.title"));
		this.parent = parent;
		this.radius = radius;
		this.apply = apply;
	}

	@Override
	protected void init() {
		this.layout.addTitleHeader(this.title, this.font);
		LinearLayout contents = this.layout.addToContents(LinearLayout.vertical().spacing(4));
		contents.defaultCellSetting().alignHorizontallyCenter();
		contents.addChild(new StringWidget(Component.translatable("createWorld.customize.finiteworld.radius"), this.font));
		EditBox field = contents.addChild(new EditBox(this.font, 200, 20, Component.translatable("createWorld.customize.finiteworld.radius")));
		field.setValue(Integer.toString(this.radius));
		contents.addChild(new StringWidget(Component.translatable("createWorld.customize.finiteworld.range", FiniteRadius.MIN, FiniteRadius.MAX), this.font));

		LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		Button done = footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.apply.accept(FiniteRadius.parse(field.getValue()).orElseThrow());
			this.onClose();
		}).build());
		footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
		field.setResponder(text -> done.active = FiniteRadius.parse(text).isPresent());

		this.layout.visitWidgets(this::addRenderableWidget);
		this.setInitialFocus(field);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}
}
