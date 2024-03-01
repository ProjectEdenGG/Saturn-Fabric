package gg.projecteden.titan.config;

import gg.projecteden.titan.config.annotations.Description;
import gg.projecteden.titan.config.annotations.Group;
import gg.projecteden.titan.config.annotations.Name;
import gg.projecteden.titan.config.annotations.OldConfig;
import gg.projecteden.titan.discord.RichPresence;
import gg.projecteden.titan.saturn.SaturnUpdater;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gg.projecteden.titan.config.annotations.Group.*;
import static gg.projecteden.titan.utils.Utils.isOnEden;

@Getter
@Setter
public class ConfigItem<T> {

    private T value;

    public ConfigItem(T defaultValue) {
        this.value = defaultValue;
    }

    public Type getType() {
        if (value instanceof Boolean)
            return Type.BOOLEAN;
        if (value instanceof Enum<?>)
            return Type.ENUM;
        if (value instanceof Integer)
            return Type.INTEGER;
        if (value instanceof Double)
            return Type.DOUBLE;
        if (value instanceof String)
            return Type.STRING;
        return Type.UNKNOWN;
    }

    public void onUpdate() {}

    public enum Type {
        BOOLEAN,
        ENUM,
        INTEGER,
        DOUBLE,
        STRING,
        UNKNOWN
    }

    public static List<Field> getAll() {
        return Arrays.stream(ConfigItem.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> f.getType() == ConfigItem.class)
                .collect(Collectors.toList());
    }

    @Group(Saturn)
    @Name(value = "Update Instances", config = "update-instances")
    @Description("When should Saturn be updated?")
    public static final ConfigItem<SaturnUpdater.Mode> SATURN_UPDATE_INSTANCES = new ConfigItem<>(SaturnUpdater.Mode.BOTH);

    @Group(Saturn)
    @Name(value = "Enabled by Default", config = "enabled-default")
    @Description("Should Saturn be enabled by default?")
    @OldConfig("saturn-enabled-default")
    public static final ConfigItem<Boolean> SATURN_ENABLED_DEFAULT = new ConfigItem<>(true);

    @Group(Saturn)
    @Name(value = "Manage Status", config = "manage-status")
    @OldConfig("saturn-manage-status")
    @Description("Should Titan enable and disable Saturn\nautomatically when playing on Project Eden?")
    public static final ConfigItem<Boolean> SATURN_MANAGE_STATUS = new ConfigItem<>(false);

    @Group(Saturn)
    @Name(value = "Hard Reset", config = "hard-reset")
    @OldConfig("saturn-hard-reset")
    @Description("""
                            Should Titan remove any local changes to Saturn on update?

                            This is a developer setting. Do not change unless you know what you're doing!""")
    public static final ConfigItem<Boolean> SATURN_HARD_RESET = new ConfigItem<>(true);

    @Group(Utilities)
    @Name("Stop Entity Culling")
    @Description("""
                            Should Titan prevent ArmorStands and ItemFrames from
                            un-rendering when inside a certain radius?

                            This prevents flickering and has no impact on performance.""")
    public static final ConfigItem<Boolean> STOP_ENTITY_CULLING = new ConfigItem<>(true);

    @Group(Utilities)
    @Name("Discord Rich Presence")
    @Description("Should we update your Discord Status while on Project Eden")
    public static final ConfigItem<Boolean> DISCORD_RICH_PRESENCE = new ConfigItem<>(true) {
        @Override
        public void onUpdate() {
            if (!isOnEden())
                return;
            if (getValue())
                RichPresence.start();
            else
                RichPresence.stop();
        }
    };

    @Group(Backpacks)
    @Name("Show Backpack Previews")
    @Description("Should Titan render previews of Backpacks\nwhen hovered in your inventory?")
    public static final ConfigItem<Boolean> DO_BACKPACK_PREVIEWS = new ConfigItem<>(true);

    @Group(Backpacks)
    @Name("Backpack Previews Require Shift-Key")
    @Description("Should Backpack previews only show\nwhile the shift-key is pressed?")
    public static final ConfigItem<Boolean> PREVIEWS_REQUIRE_SHIFT = new ConfigItem<>(true);

    @Group(Backpacks)
    @Name("Use Backpack Colors")
    @Description("Should Backpack previews use the color of the Backpack?")
    public static final ConfigItem<Boolean> USE_BACKGROUND_COLORS = new ConfigItem<>(true);

}
