package gg.projecteden.titan;

import net.fabricmc.loader.api.FabricLoader;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static gg.projecteden.titan.Utils.bash;

public class Saturn {

	private static final String command = "git pull";
	private static final Path path = Paths.get(URI.create(FabricLoader.getInstance().getGameDir().toUri() + "/resourcepacks/Saturn"));

	public static void update() {
		try {
			if (!path.toFile().exists())
				return;

			Titan.log("Updating Saturn");
			Titan.log(bash(command, path.toFile()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
