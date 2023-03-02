package gg.projecteden.titan.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static gg.projecteden.titan.Titan.UPDATE_AVAILABLE;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {

	private boolean updateAvailable;
	private String saturnVersion;

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
		this.addDrawableChild(new ButtonWidget.Builder(Text.of(""), button -> action.onPress(button)).dimensions(this.width / 2 - 180, this.height / 6 + 120 - 6, 20, 20).build());
		TexturedButtonWidget updateSaturnButton = new TexturedButtonWidget(this.width / 2 - 180, this.height / 6 + 120 - 6, 20, 20, 0, 0, 0, Titan.PE_LOGO, 20, 20, action, Text.of("Update Saturn"));
		// TODO Set this as tooltip of updateSaturnButton
        String tooltipText = "Saturn installed with Titan\n" +
            "Version: " + saturnVersion;
        if (updateAvailable) {
            tooltipText +=
                """
                    \nThere is an update available
                    Click to download
                    """;
        }
        updateSaturnButton.setTooltip(Tooltip.of(Text.literal(tooltipText)));

		this.addDrawableChild(updateSaturnButton);
		if (updateAvailable) {
			this.addDrawable((matrices, mouseX, mouseY, delta) -> {
				RenderSystem.setShaderTexture(0, UPDATE_AVAILABLE);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				matrices.push();
				matrices.scale(0.4F, 0.4F, 0.4F);
				DrawableHelper.drawTexture(matrices, (int) ((this.width / 2 - 165) * 2.5), (int) ((this.height / 6 + 120 - 15) * 2.5), 0.0F, 0.0F, 9, 40, 9, 40);
				matrices.pop();
			});
		}
	}

}
