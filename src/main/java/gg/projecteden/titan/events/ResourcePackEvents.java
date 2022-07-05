package gg.projecteden.titan.events;

import gg.projecteden.titan.network.ServerChannel;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.Utils;
import gg.projecteden.titan.saturn.SaturnUpdater;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static gg.projecteden.titan.Titan.MOD_ID;
import static gg.projecteden.titan.Utils.isOnEden;

public class ResourcePackEvents {

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
			if (isOnEden()) {
				if (Saturn.manageStatus)
					Saturn.enable();
				ServerChannel.reportToEden();
			}
		}));
		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
			if (Saturn.manageStatus)
				Saturn.disable();
		}));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (Saturn.enabledByDefault)
				Saturn.enable();
		});

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(MOD_ID, "saturn");
			}

			@Override
			public void reload(ResourceManager manager) {
				ServerChannel.reportToEden();
			}

			@Override
			public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
				if (isOnEden() && (Saturn.updater == SaturnUpdater.GIT || (Saturn.mode == SaturnUpdater.Mode.BOTH || Saturn.mode == SaturnUpdater.Mode.TEXTURE_RELOAD)))
					prepareExecutor.execute(Saturn::update);
				return SimpleSynchronousResourceReloadListener.super.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
			}
		});
	}

}
