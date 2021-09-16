package gg.projecteden.titan;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static gg.projecteden.titan.Utils.bash;

public class Saturn {

	private static final Path path = Paths.get(URI.create(FabricLoader.getInstance().getGameDir().toUri() + "/resourcepacks/Saturn"));

	public static void update() {
		try {
			if (!isInstalled())
				return;

			Titan.log("Updating Saturn");
			Titan.log(git("pull"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isInstalled() {
		return path.toFile().exists();
	}

	public static String version() {
		if (!isInstalled())
			return null;

		return git("rev-parse HEAD");
	}

	@NotNull
	private static String git(String command) {
		return bash("git " + command, path.toFile());
	}

}
