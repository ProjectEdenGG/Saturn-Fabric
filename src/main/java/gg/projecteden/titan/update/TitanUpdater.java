package gg.projecteden.titan.update;

import gg.projecteden.titan.Titan;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

import static gg.projecteden.titan.Utils.getGitResponse;

public class TitanUpdater {

	public static UpdateStatus updateStatus = UpdateStatus.NONE;
	private static GitResponse.TitanRelease latestRelease;

	public static void getMostRecent(GitResponse.TitanRelease[] releases) {
		if (releases.length == 0)
			return;
		Arrays.sort(releases, Comparator.comparing(GitResponse.TitanRelease::getCreatedAt).reversed());
		latestRelease = releases[0];
	}

	public static CompletableFuture<Boolean> downloadUpdate() {
		updateStatus = UpdateStatus.DOWNLOADING;
		CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			GitResponse.TitanRelease.Asset asset = null;
			for (GitResponse.TitanRelease.Asset _asset : latestRelease.assets) {
				if (_asset.content_type.equals("jar")) {
					asset = _asset;
					break;
				}
			}
			if (asset == null) {
				Titan.log("Asset is null");
				completableFuture.complete(false);
				return;
			}
			try {
				FileUtils.copyURLToFile(
						new URL(asset.browser_download_url),
						FabricLoader.getInstance().getGameDir().resolve("mods/titan.jar").toFile());
				completableFuture.complete(true);
			} catch (Exception ex) {
				Titan.log("An error occurred while downloading Titan:");
				ex.printStackTrace();
				completableFuture.complete(false);
			}
		});
		try {
			thread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			completableFuture.complete(false);
		}
		return completableFuture;
	}

	public static void checkForUpdates() {
		try {
			TitanUpdater.getMostRecent(getGitResponse("Titan/releases", GitResponse.TitanRelease[].class));
			if (latestRelease != null && !latestRelease.getSha().startsWith(Titan.version())) {
				updateStatus = UpdateStatus.AVAILABLE;
			}
		} catch (Exception ignore) { } // Rate limit on unauthenticated git requests
	}
}
