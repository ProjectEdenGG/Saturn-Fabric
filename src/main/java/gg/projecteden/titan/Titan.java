package gg.projecteden.titan;

import com.google.common.base.Strings;
import gg.projecteden.titan.events.Events;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public static boolean isOnEden() {
		final ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
		if (handler == null)
			return false;

		final String address = handler.getConnection().getAddress().toString();
		if (address == null)
			return false;

		return address.contains("projecteden.gg") || address.contains("51.222.11.194");
	}

	public static void reportVersions() {
		try {
			if (!isOnEden())
				return;

			final MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.player == null)
				return;

			String titanVersion = Titan.container().getMetadata().getVersion().getFriendlyString();
			String saturnVersion = null;

			final Path saturnVersionFile = Paths.get(URI.create(client.getResourcePackDir().toURI() + "/Saturn/.git/ORIG_HEAD"));
			if (saturnVersionFile.toFile().exists())
				saturnVersion = Files.readAllLines(saturnVersionFile, StandardCharsets.UTF_8).get(0);

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
