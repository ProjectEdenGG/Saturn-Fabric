package gg.projecteden.titan.update;

public abstract class GitResponse {

	public abstract String getSha();

	public static class Saturn extends GitResponse {
		public String sha;

		@Override
		public String getSha() {
			return sha;
		}
	}

}
