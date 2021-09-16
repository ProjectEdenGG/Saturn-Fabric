package gg.projecteden.titan;

import com.google.common.base.Strings;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Titan implements ModInitializer {

	@Override
	public void onInitialize() {
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
			try {
				final String address = handler.getConnection().getAddress().toString();
				boolean eden = address.contains("projecteden.gg") || address.contains("51.222.11.194");

				if (!eden)
					return;

				String titanVersion = modContainer().getMetadata().getVersion().getFriendlyString();
				String saturnVersion = null;

				final Path path = Paths.get(URI.create(client.getResourcePackDir().toURI() + "/Saturn/.git/ORIG_HEAD"));
				if (path.toFile().exists())
					saturnVersion = Files.readAllLines(path, StandardCharsets.UTF_8).get(0);

				String command = "/resourcepack versions";
				if (!Strings.isNullOrEmpty(titanVersion))
					command += " --titan=" + titanVersion;
				if (!Strings.isNullOrEmpty(saturnVersion))
					command += " --saturn=" + saturnVersion;

				client.player.sendChatMessage(command);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}));
	}

	@NotNull
	private ModContainer modContainer() {
		return FabricLoader.getInstance().getModContainer(Titan.class.getSimpleName().toLowerCase())
				.orElseThrow(() -> new RuntimeException("Titan not loaded"));
	}

}
