package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Config;
import gg.projecteden.titan.Titan;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.file.Paths;

import static gg.projecteden.titan.Utils.bash;
import static gg.projecteden.titan.saturn.Saturn.PATH;

public enum SaturnUpdater {
	ZIP_DOWNLOAD {
		@Override
		public String version() {
			return Saturn.version;
		}

		@Override
		public String install(String version) {
			return update(version);
		}

		@Override
		public String update(String version) {
			try {
				Titan.log("Updating Saturn via Zip Download");
				FileUtils.copyURLToFile(new URL("https://cdn.projecteden.gg/ResourcePack.zip"), Paths.get(PATH + ".zip").toFile());
				Titan.log("Unpacking...");

				try (ZipFile zipFile = new ZipFile(PATH + ".zip")) {
					PATH.toFile().delete();
					zipFile.extractAll(PATH.toString());
					zipFile.getFile().delete();
				}

				Saturn.version = version;
				Config.save();
				return "Successfully updated Saturn";
			} catch (Exception ex) {
				Titan.log("An error occurred while updating Saturn:");
				ex.printStackTrace();
				return null;
			}
		}
	},
	GIT {
		@Override
		public String version() {
			return git("rev-parse HEAD");
		}

		@Override
		public String update(String version) {
			Titan.log("Updating Saturn via git");
			return git("pull");
		}

		@Override
		public String install(String version) {
			return git("clone https://github.com/ProjectEdenGG/Saturn.git");
		}

		@NotNull
		private static String git(String command) {
			return bash("git " + command, PATH.toFile());
		}
	};

	public abstract String version();

	public abstract String update(String version);

	public abstract String install(String version);

	public enum Mode {
		START_UP,
		TEXTURE_RELOAD,
		BOTH
	}

}
