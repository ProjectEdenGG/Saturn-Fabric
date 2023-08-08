package gg.projecteden.titan.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.update.TitanUpdater;
import gg.projecteden.titan.update.UpdateStatus;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gg.projecteden.titan.Titan.UPDATE_AVAILABLE;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	private static ServerInfo serverInfo;

	// Just have to have this due to screen methods
	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgetsNormal")
	private void addDirectServerButton(int y, int spacingY, CallbackInfo ci) {
		if (TitleScreenMixin.serverInfo == null) {
			ServerInfo serverInfo = null;
			ServerList serverList = new ServerList(MinecraftClient.getInstance());
			String[] ignoredSubs = { "update", "test", "prespace", "old" };
			servers:
			for (int i = 0; i < serverList.size(); i++) {
				String ip = serverList.get(i).address.toLowerCase();
				if (ip.contains("projecteden.gg")) {
					for (String ignored : ignoredSubs)
						if (ip.contains(ignored))
							continue servers;
					serverInfo = serverList.get(i);
					break;
				}
			}
			if (serverInfo == null)
				serverInfo = new ServerInfo("project-eden", "projecteden.gg", false);

			TitleScreenMixin.serverInfo = serverInfo;
		}
		boolean modMenu = FabricLoader.getInstance().getModContainer("modmenu").isPresent();
		if (modMenu)
			y -= spacingY;
		ButtonWidget.PressAction action = button -> {
			if (TitanUpdater.updateStatus == UpdateStatus.AVAILABLE) {
                button.setTooltip(UpdateStatus.DOWNLOADING.getTitleScreenTooltip());
				TitanUpdater.downloadUpdate().thenAccept(bool -> {
					if (bool) {
						TitanUpdater.updateStatus = UpdateStatus.DONE;
                        button.setTooltip(UpdateStatus.DONE.getTitleScreenTooltip());
						new Thread(() -> {
							try { Thread.sleep(2000);
							} catch (InterruptedException ignore) { }
							MinecraftClient.getInstance().stop();
						}).start();
					}
					else {
                        TitanUpdater.updateStatus = UpdateStatus.ERROR;
                        button.setTooltip(UpdateStatus.ERROR.getTitleScreenTooltip());
                    }
				});
			} else if (TitanUpdater.updateStatus == UpdateStatus.NONE)
				ConnectScreen.connect(this, MinecraftClient.getInstance(), ServerAddress.parse("projecteden.gg"), TitleScreenMixin.serverInfo);
		};
		this.addDrawableChild(ButtonWidget.builder(Text.of(""), action).dimensions(this.width / 2 - 100 + 205, y + spacingY, 20, 20).build());
		TexturedButtonWidget joinProjectEdenButton = new TexturedButtonWidget(this.width / 2 - 100 + 205, y + spacingY, 20, 20, 0, 0, 0, Titan.PE_LOGO, 20, 20, action, Text.of("Project Eden"));
		joinProjectEdenButton.setTooltip(TitanUpdater.updateStatus.getTitleScreenTooltip());
        this.addDrawableChild(joinProjectEdenButton);

		if (!modMenu)
			y -= (spacingY / 2);
		int finalY = y;
		if (TitanUpdater.updateStatus != UpdateStatus.NONE) {
			this.addDrawable((matrices, mouseX, mouseY, delta) -> {
				RenderSystem.setShaderTexture(0, UPDATE_AVAILABLE);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				matrices.push();
				matrices.scale(0.4F, 0.4F, 0.4F);
				DrawContext.drawTexture(matrices, (int) ((TitleScreenMixin.this.width / 2 - 100 + 220) * 2.5), (int) ((finalY + spacingY + 5) * 2.5), 0.0F, 0.0F, 9, 40, 9, 40);
				matrices.pop();
			});
		}
	}
}
