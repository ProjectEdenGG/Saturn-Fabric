package gg.projecteden.titan;

import lombok.SneakyThrows;

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

}
