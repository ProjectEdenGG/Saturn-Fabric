package gg.projecteden.titan;

import com.google.gson.Gson;
import joptsimple.internal.Strings;
import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

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

	public static String camelCase(String text) {
		if (Strings.isNullOrEmpty(text))
			return text;

		return Arrays.stream(text.replaceAll("_", " ").split(" "))
				       .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
				       .collect(Collectors.joining(" "));
	}

	public static <T> T getGitResponse(String get, Class<T> type) {
		try {
			return new Gson().fromJson(
					bash("curl -A 'Googlebot/2.1 (+http://www.google.com/bot.html)' \\ " +
							     "-H \"Accept: application/vnd.github+json\" \\ " +
							     "https://api.github.com/repos/ProjectEdenGG/" + get,
							FabricLoader.getInstance().getGameDir().toFile()), type);
		} catch (Exception ignore) { } // Rate limit for unauthenticated git api requests
		return null;
	}

}
