package gg.projecteden.titan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;

import java.io.*;
import java.nio.file.Path;

import static gg.projecteden.titan.Titan.MOD_ID;
import static gg.projecteden.titan.Utils.bash;
import static gg.projecteden.titan.saturn.Saturn.PATH;

public class Config {

	public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final static Path GAME_CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	private final static Path TITAN_CONFIG_DIR = GAME_CONFIG_DIR.resolve(MOD_ID);

	private final static File CONFIG_FILE = new File(configDir(), MOD_ID + ".json");

	private static boolean gitInstalled = false;

	public static boolean isGitInstalled() {
		return gitInstalled;
	}

	static {
		checkGitInstalled();
	}

	static void checkGitInstalled() {
		try {
			bash("git", PATH.toFile());
			gitInstalled = true;
		} catch (Exception ignore) {}
	}

	public static File configDir() {
		File mapConfigDir = TITAN_CONFIG_DIR.toFile();
		if (!mapConfigDir.exists()) {
			mapConfigDir.mkdirs();
		}
		return mapConfigDir;
	}

	public static JsonObject getJsonObject(File jsonFile) {
		if (jsonFile.exists()) {
			JsonObject jsonObject = loadJson(jsonFile).getAsJsonObject();
			if (jsonObject == null) {
				return new JsonObject();
			}
			return jsonObject;
		}

		return new JsonObject();
	}

	public static JsonElement loadJson(File jsonFile) {
		if (jsonFile.exists()) {
			try (Reader reader = new FileReader(jsonFile)) {
				return loadJson(reader);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public static JsonElement loadJson(Reader reader) {
		return GSON.fromJson(reader, JsonElement.class);
	}

	public static void storeJson(File jsonFile, JsonElement jsonObject) {
		try (FileWriter writer = new FileWriter(jsonFile)) {
			String json = GSON.toJson(jsonObject);
			writer.write(json);
			writer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void save() {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("saturn-updater", Saturn.getUpdater().name().toLowerCase());
		jsonObject.addProperty("saturn-hard-reset", Saturn.hardReset);
		jsonObject.addProperty("saturn-update-mode", Saturn.mode.name().toLowerCase());
		jsonObject.addProperty("saturn-version", Saturn.getUpdater().version());
		jsonObject.addProperty("saturn-manage-status", Saturn.manageStatus);
		jsonObject.addProperty("saturn-enabled-default", Saturn.enabledByDefault);
		storeJson(CONFIG_FILE, jsonObject);
	}

	public static void load() {
		JsonObject json = getJsonObject(CONFIG_FILE);
		if (json.has("saturn-updater"))
			Saturn.setUpdater(SaturnUpdater.valueOf(JsonHelper.getString(json, "saturn-updater").toUpperCase()));
		if (json.has("saturn-hard-reset"))
			Saturn.hardReset = JsonHelper.getBoolean(json, "saturn-hard-reset");
		if (json.has("saturn-update-mode"))
			Saturn.mode = SaturnUpdater.Mode.valueOf(JsonHelper.getString(json, "saturn-update-mode").toUpperCase());
		if (json.has("saturn-version"))
			Saturn.version = JsonHelper.getString(json, "saturn-version");
		if (json.has("saturn-manage-status"))
			Saturn.manageStatus = JsonHelper.getBoolean(json, "saturn-manage-status");
		if (json.has("saturn-enabled-default"))
			Saturn.enabledByDefault = JsonHelper.getBoolean(json, "saturn-enabled-default");
	}


}
