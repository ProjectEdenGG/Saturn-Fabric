package gg.projecteden.titan.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.update.TitanUpdater;
import gg.projecteden.titan.update.UpdateStatus;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gg.projecteden.titan.Titan.PE_LOGO_IDEN;
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
				serverInfo = new ServerInfo("project-eden", "projecteden.gg", ServerInfo.ServerType.OTHER);

			TitleScreenMixin.serverInfo = serverInfo;
		}
		boolean modMenu = FabricLoader.getInstance().getModContainer("modmenu").isPresent();
		if (modMenu)
			y -= spacingY;
		ButtonWidget.PressAction action = button -> {
			if (TitanUpdater.updateStatus == UpdateStatus.AVAILABLE && Screen.hasShiftDown()) {
				Util.getOperatingSystem().open(Titan.MODRINTH_URL);
			} else
				ConnectScreen.connect(this, MinecraftClient.getInstance(), ServerAddress.parse("projecteden.gg"), TitleScreenMixin.serverInfo, false, null);
		};
		this.addDrawableChild(ButtonWidget.builder(Text.of(""), action)
				.dimensions(this.width / 2 - 100 + 205, y + spacingY, 20, 20)
				.tooltip(TitanUpdater.updateStatus.getTitleScreenTooltip())
				.build());

		int finalY = y;
		if (!modMenu)
			y -= (spacingY / 2);
		int finalY2 = y;

		this.addDrawable((context, mouseX, mouseY, delta) -> {
			RenderSystem.setShaderTexture(0, PE_LOGO_IDEN);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			context.getMatrices().push();
			context.getMatrices().scale(1f, 1F, 1F);
			context.drawTexture(PE_LOGO_IDEN, this.width / 2 - 100 + 205, finalY + spacingY + (spacingY / 2), 0.0F, 0.0F, 20, 20, 20, 20);
			context.getMatrices().pop();
		});

		if (TitanUpdater.updateStatus != UpdateStatus.NONE) {
			this.addDrawable((context, mouseX, mouseY, delta) -> {
				RenderSystem.setShaderTexture(0, UPDATE_AVAILABLE);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				context.getMatrices().push();
				context.getMatrices().scale(0.4F, 0.4F, 0.4F);
				context.drawTexture(UPDATE_AVAILABLE, (int) ((TitleScreenMixin.this.width / 2 - 100 + 220) * 2.5), (int) ((finalY2 + spacingY + 5) * 2.5), 0.0F, 0.0F, 9, 40, 9, 40);
				context.getMatrices().pop();
			});
		}
	}

}
