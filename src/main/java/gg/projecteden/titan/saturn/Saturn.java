package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.ServerChannel;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Saturn {

	@Getter
	private static final SaturnUpdater updater = SaturnUpdater.GIT;
	public static SaturnUpdater.Mode mode = SaturnUpdater.Mode.START_UP;
	public static SaturnUpdater.Env env = SaturnUpdater.Env.PROD;
	public static boolean hardReset = true;
	public static boolean manageStatus = false;
	public static final Path PATH = FabricLoader.getInstance().getGameDir().resolve("resourcepacks/Saturn");
	public static final Path DOT_GIT_PATH = PATH.resolve(".git");
	public static boolean enabledByDefault = true;

	public static List<Runnable> queuedProcesses = new ArrayList<>();

	public static boolean update() {
		try {
			if (!isInstalled()) {
				Titan.log("Installing Saturn");
				Titan.log(updater.install());
			} else if (updater.checkForUpdates()) {
				Titan.log("Updating Saturn");
				Titan.log(updater.update());
			} else {
				Titan.log("Not updating as Saturn is already up-to-date");
				return false;
			}
			ServerChannel.reportToEden();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean isInstalled() {
		return PATH.toFile().exists() && DOT_GIT_PATH.toFile().exists();
	}

	public static String version() {
		if (!isInstalled())
			return null;

		return updater.version();
	}

	public static String shortVersion() {
		final String version = Saturn.version();
		if (version == null)
			return null;

		return version.substring(0, Math.min(7, version.length()));
	}

	public static boolean checkForUpdates() {
		return updater.checkForUpdates();
	}

	public static void queueProcess(Runnable runnable) {
		queuedProcesses.add(runnable);
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
