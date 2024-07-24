package gg.projecteden.titan.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.models.PluginMessage;
import gg.projecteden.titan.network.models.Serverbound;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gg.projecteden.titan.Titan.MOD_ID;

public class ServerClientMessaging {

	public record TitanPacket(String packet) implements CustomPayload {
		private static final Identifier NETWORKING_CHANNEL = Identifier.of(MOD_ID, "networking");

		public static final CustomPayload.Id<TitanPacket> PACKET_ID = new CustomPayload.Id<>(NETWORKING_CHANNEL);
		public static final PacketCodec<RegistryByteBuf, TitanPacket> PACKET_CODEC = PacketCodecs.STRING.xmap(TitanPacket::new, TitanPacket::getPacket).cast();

		@Override
		public Id<? extends CustomPayload> getId() {
			return PACKET_ID;
		}

		public String getPacket() {
			return packet;
		}

		public void receive() {
			Titan.debug("Received server message: " + packet);
			JsonObject json = GSON.fromJson(packet, JsonObject.class);

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

		ClientPlayNetworking.send(new TitanPacket(GSON.toJson(json)));

		toSend.forEach(Serverbound::onSend);
		toSend.clear();
	}

	public static void init() {
		PayloadTypeRegistry.playC2S().register(TitanPacket.PACKET_ID, TitanPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(TitanPacket.PACKET_ID, TitanPacket.PACKET_CODEC);

		ClientPlayNetworking.registerGlobalReceiver(TitanPacket.PACKET_ID, (payload, context) -> payload.receive());

		ClientTickEvents.END_CLIENT_TICK.register(client -> ServerClientMessaging.flush());
	}

}

