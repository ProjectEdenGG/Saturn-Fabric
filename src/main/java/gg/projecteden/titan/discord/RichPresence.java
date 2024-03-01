package gg.projecteden.titan.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.config.ConfigItem;
import joptsimple.internal.Strings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.io.File;
import java.time.Instant;

public class RichPresence {

    static File discordLibrary;
    static boolean disabled;

    private static CreateParams params;
    static Core core;
    static Activity mainActivity;
    static Activity tempActivity;
    static boolean started;

    private static void update() {
        if (disabled) return;
        if (core == null) return;
        if (!ConfigItem.DISCORD_RICH_PRESENCE.getValue()) return;

        if (mainActivity != null || tempActivity != null)
            core.runCallbacks();
    }

    public static void start() {
        Titan.debug("Starting RPC");
        if (disabled) return;
        Titan.debug("Core was loading");
        if (core == null || !core.isOpen())
            if (!tryCreateCore()) return;
        Titan.debug("Core is not null and open");
        if (started) return;
        Titan.debug("Not started");
        if (!ConfigItem.DISCORD_RICH_PRESENCE.getValue()) return;
        Titan.debug("RPC Enabled");
        if (PlayerStates.isVanished()) return;
        Titan.debug("Not vanished");

        RichPresence.mainActivity = generateActivity();
        if (mainActivity == null) {
            Titan.debug("Main activity is null");
            return;
        }

        Titan.debug("Setting details: " + PlayerStates.getWorldDetails());
        mainActivity.setDetails(PlayerStates.getWorldDetails());

        Titan.debug("Updating activity manager");
        core.activityManager().updateActivity(mainActivity);
        started = true;
    }

    public static void stop() {
        if (disabled) return;
        if (core == null || !core.isOpen()) return;

        if (tempActivity != null)
            tempActivity.close();
        if (mainActivity != null)
            mainActivity.close();

        tempActivity = null;
        mainActivity = null;

        core.close();
        core = null;

        params.close();
        params = null;

        started = false;
    }

    public static void resetTimestamp() {
        if (disabled) return;
        if (core == null || !core.isOpen()) return;
        if (mainActivity == null) return;

        tempActivity = null;

        Titan.debug("Returning to main activity");
        core.activityManager().updateActivity(mainActivity);
    }

    public static void setTimestamp() {
        if (disabled) return;
        if (core == null || !core.isOpen()) return;
        if (mainActivity == null) return;

        Titan.debug("Switching to new activity for timestamp");
        tempActivity = generateActivity();
        if (tempActivity == null) return;

        tempActivity.setDetails(mainActivity.getDetails());
        tempActivity.setState(mainActivity.getState());

        core.activityManager().updateActivity(tempActivity);
    }

    public static void updateWorld() {
        if (disabled) return;
        if (core == null || !core.isOpen()) return;
        if (mainActivity == null) return;

        mainActivity.setDetails(PlayerStates.getWorldDetails());
        core.activityManager().updateActivity(mainActivity);
    }

    public static void updateDetails(String details) {
        if (disabled) return;
        if (core == null || !core.isOpen()) return;
        if (mainActivity == null) return;

        if (details == null)
            details = Strings.EMPTY;

        mainActivity.setState(details);
        core.activityManager().updateActivity(mainActivity);
    }

    public static Activity generateActivity() {
        if (disabled) return null;
        if (core == null) return null;

        Titan.debug("Generating activity");
        Activity activity = new Activity();
        activity.assets().setLargeImage("shield-circle");
        activity.assets().setLargeText("projecteden.gg");
        activity.timestamps().setStart(Instant.now());

        Titan.debug("Activity generated");
        return activity;
    }

    private static void initEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> RichPresence.update());

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> stop()));
    }

    public static void init() {
        try {
            Core.initDownload();

            if (tryCreateCore())
                initEvents();
        } catch (Exception ex) {
            disabled = true;
            Titan.log("There was an exception while initialize Discord Rich Presence: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static boolean tryCreateCore() {
        try {
            CreateParams params = new CreateParams();
            params.setClientID(1207154982076555274L);
            params.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);

            RichPresence.params = params;
            RichPresence.core = new Core(params);
            return true;
        } catch (Exception ex) {
            disabled = true;
            Titan.log("There was an exception while initialize Discord Rich Presence: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

//    public static File downloadDiscordLibrary() throws IOException {
//        String name = "discord_game_sdk";
//        String suffix;
//
//        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
//        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
//
//        Titan.debug("Operating System: " + osName);
//        Titan.debug("Arch: " + arch);
//
//        if (osName.contains("windows"))
//            suffix = ".dll";
//        else if (osName.contains("linux"))
//            suffix = ".so";
//        else if (osName.contains("mac os"))
//            suffix = ".dylib";
//        else
//            throw new RuntimeException("cannot determine Operating System: " + osName);
//
//        if (arch.equals("amd64"))
//            arch = "x86_64";
//
//        String zipPath = "lib/%s/%s%s".formatted(arch, name, suffix);
//
//        URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip");
//        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
//        connection.setRequestProperty("User-Agent", "discord-game-sdk4j (https://github.com/JnCrMx/discord-game-sdk4j)");
//        ZipInputStream zin = new ZipInputStream(connection.getInputStream());
//
//        ZipEntry entry;
//        while ((entry = zin.getNextEntry())!=null) {
//            if (entry.getName().equals(zipPath)) {
//                File tempDir = new File(System.getProperty("java.io.tmpdir"), "java-" + name + System.nanoTime());
//                if (!tempDir.mkdir())
//                    throw new IOException("Cannot create temporary directory");
//                tempDir.deleteOnExit();
//
//                File temp = new File(tempDir, name + suffix);
//                temp.deleteOnExit();
//
//                Files.copy(zin, temp.toPath());
//
//                zin.close();
//                return temp;
//            }
//            zin.closeEntry();
//        }
//        zin.close();
//        throw new NullPointerException("Could not find Discord Library");
//    }

}
