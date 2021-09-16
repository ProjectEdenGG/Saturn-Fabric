package gg.projecteden.titan;

import com.google.common.base.Strings;
import gg.projecteden.titan.events.Events;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static gg.projecteden.titan.Utils.isOnEden;

public class Titan implements ModInitializer {
	public static final String PREFIX = String.format("[%s] ", Titan.class.getSimpleName());
	public static Logger LOGGER = LogManager.getLogger();

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
		Saturn.update();
		Events.register();
	}

	public static void reportVersions() {
		try {
			if (!isOnEden())
				return;

			final MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.player == null)
				return;

			String titanVersion = Titan.version();
			String saturnVersion = Saturn.version();

			String command = "/resourcepack versions";
			if (!Strings.isNullOrEmpty(titanVersion))
				command += " --titan=" + titanVersion;
			if (!Strings.isNullOrEmpty(saturnVersion))
				command += " --saturn=" + saturnVersion;

			client.player.sendChatMessage(command);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
