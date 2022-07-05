package gg.projecteden.titan.network;

import com.google.gson.JsonObject;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static gg.projecteden.titan.Utils.isOnEden;

public class ServerChannel {

	private static final Identifier CHANNEL_SERVERBOUND = new Identifier("titan", "out");

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
			jsonObject.addProperty("saturn-updater", Saturn.updater.name().toLowerCase());
			jsonObject.addProperty("saturn-update-mode", Saturn.mode.name().toLowerCase());
			jsonObject.addProperty("saturn-manage-status", Saturn.manageStatus);
			jsonObject.addProperty("saturn-enabled-default", Saturn.enabledByDefault);

			ServerChannel.send(jsonObject.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

