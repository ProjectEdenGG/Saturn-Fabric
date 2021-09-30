package gg.projecteden.titan.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ServerChannel {

	private static final Identifier CHANNEL_OUT = new Identifier("titan", "out");;

	public static void send(String string) {
		PacketByteBuf packetByteBuf = PacketByteBufs.create();
		packetByteBuf.writeBytes(string.getBytes());

		ClientPlayNetworking.send(ServerChannel.CHANNEL_OUT, packetByteBuf);
	}

}

