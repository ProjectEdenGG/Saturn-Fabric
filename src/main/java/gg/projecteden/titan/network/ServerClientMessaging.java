package gg.projecteden.titan.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.models.PluginMessage;
import gg.projecteden.titan.network.models.Serverbound;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gg.projecteden.titan.Titan.MOD_ID;

public class ServerClientMessaging {

	private static final Identifier CHANNEL_SERVERBOUND = new Identifier(MOD_ID, "serverbound");
	private static final Identifier CHANNEL_CLIENTBOUND = new Identifier(MOD_ID, "clientbound");

	public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final List<Serverbound> toSend = new ArrayList<>();

	public static void send(Serverbound serverbound) {
		if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getNetworkHandler() != null)
			toSend.add(serverbound);
		else
			Titan.debug("Cannot send packets while not online");
	}

	private static void flush() {
		if (toSend.isEmpty()) return;
		if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().getNetworkHandler() == null) return;

		Collections.reverse(toSend); // Prefer newer messages

		JsonObject json = new JsonObject();
		toSend.forEach(serverbound -> {
			String type = serverbound.getType().name().toLowerCase();
			Titan.debug("Sending " + type);

			if (json.has(type)) { // Combine like messages
				JsonObject original = json.getAsJsonObject(type);
				JsonObject duplicate = GSON.fromJson(serverbound.getJson(), JsonObject.class);
				duplicate.keySet().forEach(key -> {
					if (original.has(key))
						return;
					original.add(key, duplicate.get(key));
				});
			}
			else
				json.add(serverbound.getType().name().toLowerCase(), GSON.fromJson(serverbound.getJson(), JsonObject.class));
		});

		PacketByteBuf packetByteBuf = PacketByteBufs.create();
		packetByteBuf.writeBytes(GSON.toJson(json).getBytes());

		ClientPlayNetworking.send(ServerClientMessaging.CHANNEL_SERVERBOUND, packetByteBuf);

		toSend.forEach(Serverbound::onSend);
		toSend.clear();
	}

	public static class ServerChannelReceiver implements ClientPlayNetworking.PlayChannelHandler {

		@Override
		public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			byte[] bytes = buf.array();
			String string = new String(bytes);
			Titan.debug("Received server message: " + string);
			JsonObject json = GSON.fromJson(string, JsonObject.class);

			if (json == null || json.isEmpty()) {
				Titan.debug("JSON is empty");
				return;
			}

			int processed = 0;
			for (PluginMessage message : PluginMessage.values()) {
				if (json.has(message.name().toLowerCase())) {
					message.receive(json.getAsJsonObject(message.name().toLowerCase()));
					processed++;
				}
			}
			Titan.debug("Processed %d messages".formatted(processed));
		}
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(ServerClientMessaging.CHANNEL_CLIENTBOUND, new ServerChannelReceiver());

		ClientTickEvents.END_CLIENT_TICK.register(client -> ServerClientMessaging.flush());
	}

}

