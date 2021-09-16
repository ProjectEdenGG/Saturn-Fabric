package gg.projecteden.titan.events;

import com.google.common.base.Strings;
import gg.projecteden.titan.Titan;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerJoinEvent {

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
			try {
				if (!isEden(handler))
					return;

				reportVersions(client);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}));
	}

	private static void reportVersions(MinecraftClient client) throws IOException {
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
	}

	private static boolean isEden(ClientPlayNetworkHandler handler) {
		final String address = handler.getConnection().getAddress().toString();
		return address.contains("projecteden.gg") || address.contains("51.222.11.194");
	}

}
