package gg.projecteden.titan;

import gg.projecteden.titan.events.Events;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import gg.projecteden.titan.update.TitanUpdater;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Titan implements ModInitializer {
	public static final String MOD_ID = "titan";
	public static final String PREFIX = String.format("[%s] ", Titan.class.getSimpleName());
	public static Logger LOGGER = LogManager.getLogger();
	public static final Identifier PE_LOGO = new Identifier(MOD_ID, "main-menu-button.png");
	public static final Identifier UPDATE_AVAILABLE = new Identifier(MOD_ID, "exclamation-mark.png");

	public static void log(String message, Object... objects) {
		LOGGER.log(Level.INFO, String.format(PREFIX + message, objects));
	}

	@NotNull
	public static ModContainer container() {
		return FabricLoader.getInstance().getModContainer(Titan.class.getSimpleName().toLowerCase())
				.orElseThrow(() -> new RuntimeException("Titan not loaded"));
	}

	public static String version() {
		return Titan.container().getMetadata().getVersion().getFriendlyString();
	}

	@Override
	public void onInitialize() {
		TitanUpdater.checkForUpdates();
		Config.load();
		if (Saturn.mode != SaturnUpdater.Mode.TEXTURE_RELOAD)
			Saturn.update();
		Events.register();
		if (Saturn.manageStatus && !Saturn.enabledByDefault)
			Saturn.disable();
	}

}
