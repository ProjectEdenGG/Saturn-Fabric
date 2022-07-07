package gg.projecteden.titan.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Optional;

import static gg.projecteden.titan.Utils.camelCase;
import static gg.projecteden.titan.config.Config.checkGitInstalled;
import static gg.projecteden.titan.config.Config.isGitInstalled;

public class ModMenuImpl implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuImpl::getConfigScreen;
	}

	public static Screen getConfigScreen(Screen parent) {
		checkGitInstalled();

		ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.literal("Titan Config"));
		ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

		ConfigCategory saturn = builder.getOrCreateCategory(Text.literal("Saturn"));
		saturn.addEntry(entryBuilder.startEnumSelector(Text.literal("Update Method"), SaturnUpdater.class, Saturn.getUpdater())
				.setEnumNameProvider(val -> Text.literal(camelCase(val.name())))
				.setErrorSupplier(val -> {
					if (val == SaturnUpdater.GIT && !isGitInstalled()) {
						return Optional.of(Text.literal("Git not installed"));
					}
					return Optional.empty();
				})
				.setTooltip(
						Text.literal("Using 'Git' requires git to be installed and setup"),
						Text.literal("Only use if you know what you're doing!")
				)
				.setSaveConsumer(val -> {
					if (val == SaturnUpdater.GIT && !isGitInstalled()) {
						val = SaturnUpdater.ZIP_DOWNLOAD;
						Titan.log("The Update Method was set to Git, but git does not appear to be installed. " +
								"Defaulting back to zip download.");
					}
					Saturn.setUpdater(val);
				})
				.build());

		if (Saturn.getUpdater() == SaturnUpdater.GIT) {
			saturn.addEntry(entryBuilder.startBooleanToggle(Text.literal("Hard Reset"), Saturn.hardReset)
					.setSaveConsumer(val -> Saturn.hardReset = val)
					.build());
		}

		saturn.addEntry(entryBuilder.startEnumSelector(Text.literal("Update Instances"), SaturnUpdater.Mode.class, Saturn.mode)
				.setEnumNameProvider(val -> Text.literal(camelCase(val.name())))
				.setTooltip(Text.literal("When should Saturn be updated?"))
				.setSaveConsumer(val -> Saturn.mode = val)
				.build());

		saturn.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enabled by Default"), Saturn.enabledByDefault)
				.setTooltip(Text.literal("Should Saturn be enabled by default?"))
				.setSaveConsumer(val -> Saturn.enabledByDefault = val)
				.build());

		saturn.addEntry(entryBuilder.startBooleanToggle(Text.literal("Manage Status"), Saturn.manageStatus)
				.setSaveConsumer(val -> Saturn.manageStatus = val)
				.setTooltip(
						Text.literal("Should Titan enable and disable Saturn"),
						Text.literal("automatically when playing on Project Eden?")
				)
				.build());

		builder.setDoesConfirmSave(false);
		builder.transparentBackground();
		builder.setSavingRunnable(Config::save);

		builder.setParentScreen(parent);

		return builder.build();
	}

}
