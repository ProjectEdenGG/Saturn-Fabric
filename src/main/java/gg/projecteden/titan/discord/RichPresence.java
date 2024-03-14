package gg.projecteden.titan.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.config.ConfigItem;
import joptsimple.internal.Strings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RichPresence {

    static boolean disabled;

    private static CreateParams params;
    static Core core;
    static Activity mainActivity;
    static Activity tempActivity;
    static boolean started;

    static Map<UpdateType, Runnable> updateQueues = new HashMap<>();

    private static void update() {
        if (disabled) return;
        if (core == null) return;
        if (!ConfigItem.DISCORD_RICH_PRESENCE.getValue()) return;

        if (mainActivity != null || tempActivity != null)
            runCallbacks();
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
        update(UpdateType.DETAILS, () -> mainActivity.setDetails(PlayerStates.getWorldDetails()));

        started = true;
    }

    public static void stop() {
        if (disabled) return;
        if (core == null || !core.isOpen()) return;

        try {
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
        } catch (Throwable throwable) {
            if (throwable.getMessage() != null)
                Titan.log(throwable.getMessage());
        }

        started = false;
    }

    public static void resetTimestamp() {
        Titan.debug("Returning to main activity");
        update(UpdateType.TIMESTAMP, () -> tempActivity = null);
    }

    public static void setTimestamp() {
       Titan.debug("Switching to new activity for timestamp");
        tempActivity = generateActivity();
        if (tempActivity == null) return;

        update(UpdateType.TIMESTAMP, () -> {
            tempActivity.setDetails(mainActivity.getDetails());
            tempActivity.setState(mainActivity.getState());
        });
    }

    public static void updateWorld() {
        update(UpdateType.DETAILS, () -> mainActivity.setDetails(PlayerStates.getWorldDetails()));
    }

    public static void updateDetails(String details) {
       update(UpdateType.STATE, () -> mainActivity.setState(details == null ? Strings.EMPTY : details));
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

    private static void runCallbacks() {
        if (disabled) return;
        if (core == null || !core.isOpen()) return;
        if (mainActivity == null) return;

        try {
            for (UpdateType type : UpdateType.values()) {
                Runnable runnable = updateQueues.remove(type);
                if (runnable == null) continue;
                runnable.run();
            }
            core.activityManager().updateActivity(tempActivity != null ? tempActivity : mainActivity);

            core.runCallbacks();
        } catch (Throwable throwable) {
            if (throwable.getMessage() != null)
                Titan.log(throwable.getMessage());
        }
    }

    private static void initEvents() {
        AtomicInteger tick = new AtomicInteger(0);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tick.getAndIncrement() % 5 == 0) {
                tick.set(0);
                new Thread(RichPresence::update).start();
            }
        });

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

    private static void update(UpdateType type, Runnable runnable) {
        updateQueues.put(type, runnable);
    }

    private enum UpdateType {
        STATE,
        DETAILS,
        TIMESTAMP
    }

}
