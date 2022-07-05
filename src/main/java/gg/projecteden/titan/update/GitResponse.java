package gg.projecteden.titan.update;

import java.util.List;

public abstract class GitResponse {

	public abstract String getSha();

	public static class Saturn extends GitResponse {
		public String sha;

		@Override
		public String getSha() {
			return sha;
		}
	}

	public static class TitanRelease extends GitResponse {

		String tag_name;
		public List<Asset> assets;
		public String created_at;

		@Override
		public String getSha() {
			return tag_name;
		}

		public String getCreatedAt() {
			return created_at;
		}

		public static class Asset {
			public String name;
			public String content_type;
			public String browser_download_url;
		}

	}

}
