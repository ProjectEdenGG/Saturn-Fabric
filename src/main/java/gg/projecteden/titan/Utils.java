package gg.projecteden.titan;

import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

public class Utils {

	@SneakyThrows
	public static String bash(String command, File directory) {
		InputStream result = Runtime.getRuntime().exec(command, null, directory).getInputStream();
		StringBuilder builder = new StringBuilder();
		new Scanner(result).forEachRemaining(string -> builder.append(string).append(" "));
		return builder.toString().trim();
	}

	public static boolean isOnEden() {
		final ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
		if (handler == null)
			return false;

		final String address = handler.getConnection().getAddress().toString();
		if (address == null)
			return false;

		return address.contains("projecteden.gg") || address.contains("51.222.11.194");
	}

}
