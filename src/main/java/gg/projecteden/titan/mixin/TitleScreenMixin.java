package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	private static ServerInfo serverInfo;
	private static final Identifier PE_LOGO = new Identifier("titan", "main-menu-button.png");

	// Just have to have this due to screen methods
	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgetsNormal")
	private void addDirectServerButton(int y, int spacingY, CallbackInfo ci) {
		ButtonWidget.TooltipSupplier tooltipSupplier = (button, matrices, mouseX, mouseY) -> {
			this.renderOrderedTooltip(
					matrices,
					MinecraftClient.getInstance().textRenderer.wrapLines(StringVisitable.plain("Connect directly to projecteden.gg"), 125),
					mouseX,
					mouseY);
		};
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
					Titan.log("Found existing ServerInfo for projecteden.gg at index %d", i);
					break;
				}
			}
			if (serverInfo == null)
				serverInfo = new ServerInfo("project-eden", "projecteden.gg", false);

			TitleScreenMixin.serverInfo = serverInfo;
		}
		if (FabricLoader.getInstance().getModContainer("modmenu").isPresent())
			y -= spacingY;
		this.addDrawableChild(new ButtonWidget(this.width / 2 - 100 + 205, y + spacingY, 20, 20, Text.of(""), button -> {
			ConnectScreen.connect(this, MinecraftClient.getInstance(), ServerAddress.parse("projecteden.gg"), TitleScreenMixin.serverInfo);
		}));
		this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 100 + 205, y + spacingY, 20, 20, 0, 0, 0, TitleScreenMixin.PE_LOGO, 20, 20, (button) -> {
			ConnectScreen.connect(this, MinecraftClient.getInstance(), ServerAddress.parse("projecteden.gg"), TitleScreenMixin.serverInfo);
		}, tooltipSupplier, Text.of("Project Eden")));
	}

}
