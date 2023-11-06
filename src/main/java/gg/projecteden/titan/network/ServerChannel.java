package gg.projecteden.titan.network;

import com.google.gson.JsonObject;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Objects;

import static gg.projecteden.titan.Titan.MOD_ID;
import static gg.projecteden.titan.Utils.isOnEden;

public class ServerChannel {

	private static final Identifier CHANNEL_SERVERBOUND = new Identifier(MOD_ID, "serverbound");
	private static final Identifier CHANNEL_CLIENTBOUND = new Identifier(MOD_ID, "clientbound");

	static {
		ClientPlayNetworking.registerGlobalReceiver(ServerChannel.CHANNEL_CLIENTBOUND, new ServerChannelReceiver());
	}

	public static void send(String string) {
		PacketByteBuf packetByteBuf = PacketByteBufs.create();
		packetByteBuf.writeBytes(string.getBytes());

		ClientPlayNetworking.send(ServerChannel.CHANNEL_SERVERBOUND, packetByteBuf);
	}

	public static void reportToEden() {
		try {
			if (!isOnEden())
				return;

			final MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.player == null)
				return;

			String titanVersion = Titan.version();
			String saturnVersion = Saturn.version();

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("titan", titanVersion);
			jsonObject.addProperty("saturn", saturnVersion);
			jsonObject.addProperty("saturn-hard-reset", Saturn.hardReset);
			jsonObject.addProperty("saturn-update-mode", Saturn.mode.name().toLowerCase());
			jsonObject.addProperty("saturn-manage-status", Saturn.manageStatus);
			jsonObject.addProperty("saturn-enabled-default", Saturn.enabledByDefault);

			ServerChannel.send(jsonObject.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static class ServerChannelReceiver implements ClientPlayNetworking.PlayChannelHandler {

		@Override
		public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			Objects.requireNonNull(PluginMessageEvent.from(new String(buf.readByteArray()))).onReceive();
		}
	}

}

