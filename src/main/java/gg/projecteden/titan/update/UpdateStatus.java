package gg.projecteden.titan.update;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public enum UpdateStatus {
	NONE(Tooltip.of(Text.of("Connect directly to projecteden.gg"))),
	AVAILABLE(Tooltip.of(Text.of("An update for Titan is available. Click to update automatically. This will close your game."))),
	DOWNLOADING(Tooltip.of(Text.of("Downloading..."))),
	ERROR(Tooltip.of(Text.of("An error occurred while updating Titan. Please report this to the Project Eden Staff."))),
	DONE(Tooltip.of(Text.of("Download complete. Stopping game...")));

	final Tooltip titleScreenTooltip;

	UpdateStatus(Tooltip titleScreenTooltip) {
		this.titleScreenTooltip = titleScreenTooltip;
	}

	public Tooltip getTitleScreenTooltip() {
		return this.titleScreenTooltip;
	}

}
