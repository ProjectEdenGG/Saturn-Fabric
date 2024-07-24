package gg.projecteden.titan.events;

import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.serverbound.Handshake;
import gg.projecteden.titan.network.serverbound.TitanConfig;
import gg.projecteden.titan.network.serverbound.Versions;
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
import static gg.projecteden.titan.config.ConfigItem.*;
import static gg.projecteden.titan.utils.Utils.isOnEden;

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
				if (SATURN_MANAGE_STATUS.getValue())
					Saturn.enable();
				ServerClientMessaging.send(new Handshake());
				ServerClientMessaging.send(new Versions());
				ServerClientMessaging.send(new TitanConfig());
			}
		}));
		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
			TitanUpdater.checkForUpdates();
			Saturn.env = SaturnUpdater.Env.PROD;
			if (SATURN_MANAGE_STATUS.getValue())
				Saturn.disable();
		}));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (SATURN_ENABLED_DEFAULT.getValue())
				Saturn.enable();
		});

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Identifier.of(MOD_ID, "saturn");
			}

			long lastForcedReload = 0L;

			@Override
			public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
				if (isOnEden() && (Saturn.getUpdater() == SaturnUpdater.GIT || SATURN_UPDATE_INSTANCES.getValue() != SaturnUpdater.Mode.START_UP)) {
					Saturn.queueProcess(() -> {
						if (Saturn.update()) {
							long thisReload = System.currentTimeMillis(); // Cooldown on forced reload. Should hopefully solve infinite loops
							if (thisReload - lastForcedReload < 30000)
								return;
							lastForcedReload = thisReload;
							MinecraftClient.getInstance().reloadResources();
							MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
						}

						ServerClientMessaging.send(new Versions());
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
