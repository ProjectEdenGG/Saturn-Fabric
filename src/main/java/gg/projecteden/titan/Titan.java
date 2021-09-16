package gg.projecteden.titan;

import gg.projecteden.titan.events.Events;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Titan implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger();

	public static void log(String message, Object... objects) {
		LOGGER.log(Level.INFO, String.format(message, objects));
	}

	@NotNull
	public static ModContainer container() {
		return FabricLoader.getInstance().getModContainer(Titan.class.getSimpleName().toLowerCase())
				.orElseThrow(() -> new RuntimeException("Titan not loaded"));
	}

	@Override
	public void onInitialize() {
		Saturn.update();
		Events.register();
	}

}
