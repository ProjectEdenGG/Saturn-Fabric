package gg.projecteden.titan.network;

import gg.projecteden.titan.saturn.Saturn;
import lombok.AllArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@AllArgsConstructor
public enum PluginMessageEvent {
	SATURN_UPDATE("saturn-update") {

		final ClickEvent clickEvent = new ClickEvent(Action.RUN_COMMAND, "/titanclient update");

		final Text text = Text.literal("")
				.append(Text.literal("[").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
				.formatted(Formatting.RESET)
				.append(Text.literal("Titan").formatted(Formatting.YELLOW))
				.append(Text.literal("]").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
				.formatted(Formatting.RESET)
				.append(Text.literal(" An update for Saturn is available. ").formatted(Formatting.DARK_AQUA))
				.append(Text.literal("Update in the Options menu or ").formatted(Formatting.DARK_AQUA))
				.append(Text.literal("Click Here").setStyle(Style.EMPTY.withClickEvent(this.clickEvent)).formatted(Formatting.YELLOW, Formatting.BOLD));

		@Override
		public void onReceive() {
			if (Saturn.checkForUpdates())
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
		}

	};

	final String id;

	public static PluginMessageEvent from(String id) {
		for (PluginMessageEvent event : values())
			if (event.id.equals(id))
				return event;
		return null;
	}

	public abstract void onReceive();


}
