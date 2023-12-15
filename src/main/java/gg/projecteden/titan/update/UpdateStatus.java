package gg.projecteden.titan.update;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public enum UpdateStatus {
	NONE(Tooltip.of(Text.of("Connect directly to projecteden.gg"))),
	AVAILABLE(Tooltip.of(Text.of("An update for Titan is available. Shift-Click to open the mod's page")));

	final Tooltip titleScreenTooltip;

	UpdateStatus(Tooltip titleScreenTooltip) {
		this.titleScreenTooltip = titleScreenTooltip;
	}

	public Tooltip getTitleScreenTooltip() {
		return this.titleScreenTooltip;
	}

}
