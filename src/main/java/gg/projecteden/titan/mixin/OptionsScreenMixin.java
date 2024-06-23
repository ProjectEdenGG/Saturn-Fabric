package gg.projecteden.titan.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gg.projecteden.titan.Titan.PE_LOGO_IDEN;
import static gg.projecteden.titan.Titan.UPDATE_AVAILABLE;

@Mixin(PackScreen.class)
public class OptionsScreenMixin extends Screen {

	private boolean updateAvailable;
	private String saturnVersion;
	private Element button;

	@Shadow ThreePartsLayoutWidget layout;

	ButtonWidget.PressAction action = button -> {
		if (updateAvailable) {
			Saturn.queueProcess(() -> {
				if (Saturn.update())
					MinecraftClient.getInstance().reloadResources();
			});
			MinecraftClient.getInstance().reloadResources();
		}
	};

	protected OptionsScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	public void drawSaturnUpdateChecker(CallbackInfo ci) {
		updateAvailable = Saturn.checkForUpdates();
		saturnVersion = Saturn.shortVersion();

		String tooltipText = "Saturn installed with Titan\n" +
				"Version: " + saturnVersion;
		if (updateAvailable) {
			tooltipText +=
					"""
                        \nThere is an update available
                        Click to download
                        """;
		}

		this.button = this.addDrawableChild(ButtonWidget.builder(Text.of(""), button -> action.onPress(button))
				.dimensions(this.width - 26, 6, 20, 20)
				.tooltip(Tooltip.of(Text.literal(tooltipText)))
				.build());

		this.addDrawable((context, mouseX, mouseY, delta) -> {
			RenderSystem.setShaderTexture(0, PE_LOGO_IDEN);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			context.getMatrices().push();
			context.getMatrices().scale(1f, 1F, 1F);
			context.drawTexture(PE_LOGO_IDEN, this.width - 26, 6, 0.0F, 0.0F, 20, 20, 20, 20);
			context.getMatrices().pop();
		});

		if (updateAvailable) {
			this.addDrawable((context, mouseX, mouseY, delta) -> {
				RenderSystem.setShaderTexture(0, UPDATE_AVAILABLE);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				context.getMatrices().push();
				context.getMatrices().scale(0.4F, 0.4F, 0.4F);
				context.drawTexture(UPDATE_AVAILABLE, (int) (this.width * (1 / .4f)) - 26, 6, 0.0F, 0.0F, 9, 40, 9, 40);
				context.getMatrices().pop();
			});
		}
	}

}
