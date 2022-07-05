package gg.projecteden.titan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import lombok.Getter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;

import static gg.projecteden.titan.Titan.MOD_ID;
import static gg.projecteden.titan.Utils.bash;
import static gg.projecteden.titan.Utils.camelCase;
import static gg.projecteden.titan.saturn.Saturn.PATH;

public class Config implements ModMenuApi {

	public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final static Path GAME_CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	private final static Path TITAN_CONFIG_DIR = GAME_CONFIG_DIR.resolve(MOD_ID);

	private final static File CONFIG_FILE = new File(configDir(), MOD_ID + ".json");

	@Getter
	private static boolean gitInstalled = false;

	static {
		checkGitInstalled();
	}

	private static void checkGitInstalled() {
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

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return Config::getConfigScreen;
	}

	public static Screen getConfigScreen(Screen parent) {
		checkGitInstalled();

		ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.literal("Titan Config"));
		ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

		ConfigCategory saturn = builder.getOrCreateCategory(Text.literal("Saturn"));
		saturn.addEntry(entryBuilder.startEnumSelector(Text.literal("Update Method"), SaturnUpdater.class, Saturn.getUpdater())
								.setEnumNameProvider(val -> Text.literal(camelCase(val.name())))
								.setErrorSupplier(val -> {
									if (val == SaturnUpdater.GIT && !gitInstalled) {
										return Optional.of(Text.literal("Git not installed"));
									}
									return Optional.empty();
								})
								.setTooltip(
										Text.literal("Using 'Git' requires git to be installed and setup"),
										Text.literal("Only use if you know what you're doing!")
								)
								.setSaveConsumer(val -> {
									if (val == SaturnUpdater.GIT && !gitInstalled) {
										val = SaturnUpdater.ZIP_DOWNLOAD;
										Titan.log("The Update Method was set to Git, but git does not appear to be installed. " +
															"Defaulting back to zip download.");
									}
									Saturn.setUpdater(val);
								})
								.build());

		if (Saturn.getUpdater() == SaturnUpdater.GIT) {
			saturn.addEntry(entryBuilder.startBooleanToggle(Text.literal("Hard Reset"), Saturn.hardReset)
					                .setSaveConsumer(val -> Saturn.hardReset = val)
					                .build());
		}

		saturn.addEntry(entryBuilder.startEnumSelector(Text.literal("Update Instances"), SaturnUpdater.Mode.class, Saturn.mode)
								.setEnumNameProvider(val -> Text.literal(camelCase(val.name())))
								.setTooltip(Text.literal("When should Saturn be updated?"))
								.setSaveConsumer(val -> Saturn.mode = val)
								.build());

		saturn.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enabled by Default"), Saturn.enabledByDefault)
								.setTooltip(Text.literal("Should Saturn be enabled by default?"))
								.setSaveConsumer(val -> Saturn.enabledByDefault = val)
								.build());

		saturn.addEntry(entryBuilder.startBooleanToggle(Text.literal("Manage Status"), Saturn.manageStatus)
								.setSaveConsumer(val -> Saturn.manageStatus = val)
								.setTooltip(
									Text.literal("Should Titan enable and disable Saturn"),
									Text.literal("automatically when playing on Project Eden?")
								)
								.build());

		builder.setDoesConfirmSave(false);
		builder.transparentBackground();
		builder.setSavingRunnable(Config::save);

		builder.setParentScreen(parent);

		return builder.build();
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
