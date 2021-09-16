package gg.projecteden.titan.events;

import gg.projecteden.titan.Saturn;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

			@Override
			public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
				if (Utils.isOnEden())
					prepareExecutor.execute(Saturn::update);
				return SimpleSynchronousResourceReloadListener.super.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
			}
		});
	}

}
