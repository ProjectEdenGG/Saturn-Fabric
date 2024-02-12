package gg.projecteden.titan.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import gg.projecteden.titan.config.annotations.Description;
import gg.projecteden.titan.config.annotations.Disabled;
import gg.projecteden.titan.config.annotations.Group;
import gg.projecteden.titan.config.annotations.Name;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static gg.projecteden.titan.utils.Utils.camelCase;

public class ModMenuImpl implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuImpl::getConfigScreen;
	}

	public static Screen getConfigScreen(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.literal("Titan Config"));
		ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

		Map<String, ConfigCategory> categories = new LinkedHashMap<>();

		for (Field field : ConfigItem.getAll()) {
			try {
				if (field.isAnnotationPresent(Disabled.class))
					continue;

				ConfigItem item = (ConfigItem) field.get(null);
				Text name = Text.literal(field.isAnnotationPresent(Name.class) ? field.getAnnotation(Name.class).value() : field.getName());
				Text description = field.isAnnotationPresent(Description.class) ? Text.literal(field.getAnnotation(Description.class).value()) : null;

				AbstractFieldBuilder selector = switch (item.getType()) {
					case BOOLEAN -> entryBuilder.startBooleanToggle(name, (Boolean) item.getValue());
					case ENUM -> entryBuilder.startEnumSelector(name, (Class) item.getValue().getClass(), (Enum<?>) item.getValue()).setEnumNameProvider(val -> Text.literal(camelCase(((Enum) val).name())));
					case INTEGER -> entryBuilder.startIntField(name, (Integer) item.getValue());
					case DOUBLE -> entryBuilder.startDoubleField(name, (Double) item.getValue());
					case STRING -> entryBuilder.startStrField(name, (String) item.getValue());
					case UNKNOWN -> null;
				};

				if (selector == null)
					continue;

				selector.setSaveConsumer(item::setValue);
				selector.setTooltip(description);

				categories.computeIfAbsent(field.isAnnotationPresent(Group.class) ? field.getAnnotation(Group.class).value() : "Default",
						cat -> builder.getOrCreateCategory(Text.literal(cat)))
						.addEntry(selector.build());

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		builder.setDoesConfirmSave(false);
		builder.transparentBackground();
		builder.setSavingRunnable(Config::save);

		builder.setParentScreen(parent);

		return builder.build();
	}

}
