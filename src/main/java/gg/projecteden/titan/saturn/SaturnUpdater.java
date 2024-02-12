package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.update.GitResponse;
import joptsimple.internal.Strings;
import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

import static gg.projecteden.titan.Utils.getGitResponse;
import static gg.projecteden.titan.saturn.Saturn.PATH;

public enum SaturnUpdater {
	GIT {
		final String REMOTE_NAME = "https";
		final String REMOTE_URI = "https://github.com/ProjectEdenGG/Saturn.git";
		boolean updateAvailable;

		@Override
		@SneakyThrows
		public String version() {
			try (Git git = git()) {
				return git.getRepository().findRef("HEAD").getObjectId().getName().substring(0, 7);
			} catch (Exception ex) {
				ex.printStackTrace();
				return "Unknown";
			}
		}

		@Override
		public String install() {
			try {
				if (PATH.toFile().exists())
					FileUtils.deleteDirectory(PATH.toFile());
				try (Git git = cloneCommand().call()) {
					return git.toString();
				}
			} catch (Exception ex) {
				Titan.log("An error occurred while installing Saturn:");
				ex.printStackTrace();
				return ex.getMessage();
			}
		}

		@SneakyThrows
		protected CloneCommand cloneCommand() {
			return Git.cloneRepository()
					.setURI(REMOTE_URI)
					.setDirectory(getResourcePackFolder().resolve("Saturn").toFile());
		}

		@NotNull
		private Path getResourcePackFolder() {
			return FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
		}

		@Override
		@SneakyThrows
		public String update() {
			Titan.log("Updating Saturn via jgit");
			try (Git git = git()) {
				if (ConfigItem.SATURN_HARD_RESET.getValue())
					git.reset().setMode(ResetType.HARD).setRef("origin/" + git.getRepository().getBranch()).call();

				updateAvailable = false;

				final List<RemoteConfig> remotes = git.remoteList().call();

				if (remotes.stream().noneMatch(config -> config.getName().equals(REMOTE_NAME)))
					git.remoteAdd().setName(REMOTE_NAME).setUri(new URIish(REMOTE_URI)).call();
				else
					git.remoteSetUrl().setRemoteName(REMOTE_NAME).setRemoteUri(new URIish(REMOTE_URI)).call();

				return git.pull().setRemote(REMOTE_NAME).setRebase(true).call().toString();
			}
		}

		@NotNull
		@SneakyThrows
		private Git git() {
			return new Git(new FileRepositoryBuilder().setGitDir(getResourcePackFolder().resolve("Saturn").resolve(".git").toFile())
					.readEnvironment()
					.findGitDir()
					.build());
		}

		@Override
		public boolean checkForUpdates() {
			if (updateAvailable)
				return true;
			else {
				try (Git git  = git()) {
					String commitVersion = getGitResponse("Saturn/commits/" + git.getRepository().getBranch(), GitResponse.Saturn.class).getSha();
					String saturnVersion = Saturn.version();
					updateAvailable = (commitVersion != null && saturnVersion != null && !commitVersion.startsWith(saturnVersion)) || Strings.isNullOrEmpty(saturnVersion);
				} catch (Exception ignore) { } // Rate limit on unauthenticated git api requests
			}
			return updateAvailable;
		}
	};

	public abstract String version();

	public abstract String install();

	public abstract String update();

	public abstract boolean checkForUpdates();

	public enum Mode {
		START_UP,
		TEXTURE_RELOAD,
		BOTH
	}

	public enum Env {
		PROD {
			@Override
			public String getSuffix() {
				return "";
			}
		},
		TEST,
		;

		public String getSuffix() {
			return "-" + name();
		}
	}

}
