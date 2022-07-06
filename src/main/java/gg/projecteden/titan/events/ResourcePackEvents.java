package gg.projecteden.titan.events;

import gg.projecteden.titan.network.ServerChannel;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import gg.projecteden.titan.update.TitanUpdater;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static gg.projecteden.titan.Titan.MOD_ID;
import static gg.projecteden.titan.Utils.isOnEden;

public class ResourcePackEvents {

	static final Text text = Text.literal("")
			                  .append(Text.literal("[").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
			                  .formatted(Formatting.RESET)
			                  .append(Text.literal("Titan").formatted(Formatting.YELLOW))
			                  .append(Text.literal("]").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
			                  .formatted(Formatting.RESET)
			                  .append(Text.literal(" Saturn was updated during your last textures reload!").formatted(Formatting.DARK_AQUA));

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
			if (isOnEden()) {
				Saturn.env = handler.getConnection().getAddress().toString().contains("25565") ? SaturnUpdater.Env.PROD : SaturnUpdater.Env.TEST;
				if (Saturn.manageStatus)
					Saturn.enable();
				ServerChannel.reportToEden();
			}
		}));
		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
			TitanUpdater.checkForUpdates();
			Saturn.env = SaturnUpdater.Env.PROD;
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
			public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
				if (isOnEden() && (Saturn.getUpdater() == SaturnUpdater.GIT || (Saturn.mode == SaturnUpdater.Mode.BOTH || Saturn.mode == SaturnUpdater.Mode.TEXTURE_RELOAD))) {
					Saturn.queueProcess(() -> {
						if (Saturn.update()) {
							MinecraftClient.getInstance().reloadResources();
							MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
							ServerChannel.reportToEden();
						}
					});
				}
				return SimpleSynchronousResourceReloadListener.super.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
			}

			@Override
			public void reload(ResourceManager manager) {
			}
		});
	}

}
