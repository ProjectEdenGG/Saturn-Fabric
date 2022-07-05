package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Config;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.ServerChannel;
import gg.projecteden.titan.update.GitResponse;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static gg.projecteden.titan.Utils.getGitResponse;

public class Saturn {

	public static String version;
	public static final String URI_STRING = FabricLoader.getInstance().getGameDir().toUri() + "/resourcepacks/Saturn";
	public static SaturnUpdater.Mode mode = SaturnUpdater.Mode.START_UP;
	public static boolean manageStatus = false;
	public static final Path PATH = Paths.get(URI.create(URI_STRING));
	public static final Path DOT_GIT_PATH = Paths.get(URI.create(URI_STRING + "/.git"));
	public static SaturnUpdater updater;
	public static boolean enabledByDefault = true;

	public static void update() {
		if (updater == null)
			if (DOT_GIT_PATH.toFile().exists() && Config.isGitInstalled())
				updater = SaturnUpdater.GIT;
			else
				updater = SaturnUpdater.ZIP_DOWNLOAD;

		String commitVersion = getGitResponse("Saturn/commits/main", GitResponse.Saturn.class).getSha();
		try {
			if (!isInstalled()) {
				Titan.log("Installing Saturn");
				Titan.log(updater.install(commitVersion));
			} else if (!commitVersion.equals(updater.version())) {
				Titan.log("Updating Saturn");
				Titan.log(updater.update(commitVersion));
			}
			ServerChannel.reportToEden();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isInstalled() {
		return PATH.toFile().exists();
	}

	public static String version() {
		if (!isInstalled())
			return null;

		return updater.version();
	}

	public static void enable() {
		ResourcePackManager manager = MinecraftClient.getInstance().getResourcePackManager();
		if (!manager.getEnabledNames().contains("file/Saturn")) {
			List<String> packs = new ArrayList<>(manager.getEnabledNames());
			packs.add("file/Saturn");
			manager.setEnabledProfiles(packs);
			MinecraftClient.getInstance().reloadResources();
		}
	}

	public static void disable() {
		ResourcePackManager manager = MinecraftClient.getInstance().getResourcePackManager();
		if (manager.getEnabledNames().contains("file/Saturn")) {
			List<String> packs = new ArrayList<>(manager.getEnabledNames());
			packs.remove("file/Saturn");
			manager.setEnabledProfiles(packs);
			MinecraftClient.getInstance().reloadResources();
		}
	}

}
