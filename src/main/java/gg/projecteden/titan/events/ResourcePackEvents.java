package gg.projecteden.titan.events;

import gg.projecteden.titan.Titan;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class ResourcePackEvents {

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> Titan.reportVersions()));

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier("titan", "saturn");
			}

			@Override
			public void reload(ResourceManager manager) {
				Titan.reportVersions();
			}
		});
	}

}
