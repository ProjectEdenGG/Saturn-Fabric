package gg.projecteden.titan.update;

public enum UpdateStatus {
	NONE("Connect directly to projecteden.gg"),
	AVAILABLE("An update for Titan is available. Click to update automatically. This will close your game."),
	DOWNLOADING("Downloading..."),
	ERROR("An error occurred while updating Titan. Please report this to the Project Eden Staff."),
	DONE("Download complete. Stopping game...");

	final String titleScreenTooltip;

	UpdateStatus(String titleScreenTooltip) {
		this.titleScreenTooltip = titleScreenTooltip;
	}

	public String getTitleScreenTooltip() {
		return this.titleScreenTooltip;
	}

}
