package gg.projecteden.titan.update;

public enum UpdateStatus {
	NONE("Connect directly to projecteden.gg"),
	AVAILABLE("An update for Titan is available. Click to download."),
	DOWNLOADING("Downloading..."),
	ERROR("An error occurred while updating Titan. Please report this to the Project Eden Staff."),
	DONE("Download complete. Please restart your game for the newest version.");

	final String titleScreenTooltip;

	UpdateStatus(String titleScreenTooltip) {
		this.titleScreenTooltip = titleScreenTooltip;
	}

	public String getTitleScreenTooltip() {
		return this.titleScreenTooltip;
	}

}
