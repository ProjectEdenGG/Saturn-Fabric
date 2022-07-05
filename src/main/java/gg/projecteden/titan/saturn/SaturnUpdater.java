package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Config;
import gg.projecteden.titan.Titan;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
		public String update(String version) {
			try {
				FileUtils.copyURLToFile(new URL("https://cdn.projecteden.gg/ResourcePack.zip"), Paths.get(PATH + ".zip").toFile());
			} catch (Exception ex) {
				Titan.log("An error occurred while downloading Saturn:");
				ex.printStackTrace();
				return null;
			}
			try {
				Titan.log("Unpacking...");
				this.unzip();
			} catch (Exception ex) {
				Titan.log("An error occurred while unpacking Saturn:");
				ex.printStackTrace();
				return null;
			}
			Saturn.version = version;
			Config.save();
			return "Successfully updated Saturn";
		}

		private void unzip() {
			try (ZipFile zipFile = new ZipFile(PATH + ".zip")) {
				zipFile.extractAll(PATH.toString());
				zipFile.getFile().delete();
			} catch (IOException e) {
				throw new RuntimeException(e);
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
			return git("pull");
		}

		@Override
		public String downloadFirst(String version) {
			return git("clone git@github.com:ProjectEdenGG/Saturn.git");
		}

		@NotNull
		private static String git(String command) {
			return bash("git " + command, PATH.toFile());
		}
	};

	public abstract String version();

	public abstract String update(String version);

	public String downloadFirst(String version) {
		return this.update(version);
	}

	public enum Mode {
		START_UP,
		TEXTURE_RELOAD,
		BOTH
	}

}
