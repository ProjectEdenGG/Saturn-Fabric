package gg.projecteden.titan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import static gg.projecteden.titan.Titan.MOD_ID;

public class Config {

	public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final static Path GAME_CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	private final static Path TITAN_CONFIG_DIR = GAME_CONFIG_DIR.resolve(MOD_ID);

	private final static File CONFIG_FILE = new File(configDir(), MOD_ID + ".json");

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

		jsonObject.addProperty("saturn-hard-reset", Saturn.hardReset);
		jsonObject.addProperty("saturn-update-mode", Saturn.mode.name().toLowerCase());
		jsonObject.addProperty("saturn-manage-status", Saturn.manageStatus);
		jsonObject.addProperty("saturn-enabled-default", Saturn.enabledByDefault);
		storeJson(CONFIG_FILE, jsonObject);
	}

	public static void load() {
		JsonObject json = getJsonObject(CONFIG_FILE);
		if (json.has("saturn-hard-reset"))
			Saturn.hardReset = JsonHelper.getBoolean(json, "saturn-hard-reset");
		if (json.has("saturn-update-mode"))
			Saturn.mode = SaturnUpdater.Mode.valueOf(JsonHelper.getString(json, "saturn-update-mode").toUpperCase());
		if (json.has("saturn-manage-status"))
			Saturn.manageStatus = JsonHelper.getBoolean(json, "saturn-manage-status");
		if (json.has("saturn-enabled-default"))
			Saturn.enabledByDefault = JsonHelper.getBoolean(json, "saturn-enabled-default");
	}

}
