package gg.projecteden.titan.discord;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static gg.projecteden.titan.utils.Utils.camelCase;

public class PlayerStates {

    @Getter
    private static String worldGroup;
    @Getter
    @Setter
    private static String mode;
    @Getter
    private static boolean vanished;
    @Getter
    private static boolean AFK;
    @Getter
    @Setter
    private static ChatChannel channel = ChatChannel.UNKNOWN;

    public static void setVanished(boolean vanished) {
        boolean oldVanished = PlayerStates.vanished;
        PlayerStates.vanished = vanished;

        if (!vanished && oldVanished)
            RichPresence.start();

        if (vanished)
            RichPresence.stop();
    }

    public static void setAFK(boolean afk) {
        PlayerStates.AFK = afk;
        RichPresence.updateWorld();

        if (afk)
            RichPresence.setTimestamp();
        else
            RichPresence.resetTimestamp();
    }

    public static void setWorldGroup(String worldGroup) {
        PlayerStates.worldGroup = worldGroup;
        RichPresence.updateWorld();
    }

    public static String getWorldDetails() {
        String details = "";
        if (AFK)
            details += "AFK ";
        details += "in " + transformWorldGroup();

        return details;
    }

    public static String transformWorldGroup() {
        if (worldGroup == null)
            return "Unknown";
        return switch (worldGroup.toLowerCase()) {
            case "staff" -> "a Staff World";
            case "server" -> "Hub";
            default -> camelCase(worldGroup);
        };
    }

    @Getter
    @AllArgsConstructor
    public enum ChatChannel {
        GLOBAL("#00aa00", "g"),
        LOCAL("#ffff55", "l"),
        STAFF("#000000", "s"),
        OPERATOR("#00aaaa", "o"),
        ADMIN("#5555ff", "a"),
        MINIGAMES("#00aaaa", "m"),
        CREATIVE("#55ffff", "c"),
        SKYBLOCK("#ffaa00", "b"),
        PARTY("#ff55ff", "p"),
        PRIVATE("#55ffff", null),
        UNKNOWN(null, null);

        final String hex;
        String shortcut;

        public int getColor() {
            int rgb = Integer.parseInt(hex.substring(1), 16);
            // Add the alpha channel (fully opaque: 0xFF)
            return (0xFF << 24) | rgb;
        }
    }

}
