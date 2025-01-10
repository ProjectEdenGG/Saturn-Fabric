package gg.projecteden.titan.network.clientbound;

import gg.projecteden.titan.discord.PlayerStates;
import gg.projecteden.titan.discord.RichPresence;
import gg.projecteden.titan.network.models.Clientbound;

public class UpdateState extends Clientbound {

    String mode;
    String worldGroup;
    String arena;
    String mechanic;
    Boolean vanished;
    Boolean afk;
    String channel;

    @Override
    public void onReceive() {
        if (vanished != null)
            PlayerStates.setVanished(vanished);

        if (mode != null)
            PlayerStates.setMode(mode);

        if (worldGroup != null) {
            PlayerStates.setWorldGroup(worldGroup);
            RichPresence.updateWorld();
        }

        if (mechanic != null) {
            String playing = "Playing " + mechanic;
            if (arena != null)
                playing += " on " + arena;

            RichPresence.updateDetails(playing);
            RichPresence.setTimestamp();
        }

        if (afk != null)
            PlayerStates.setAFK(afk);

        if (channel != null)
            try { PlayerStates.setChannel(PlayerStates.ChatChannel.valueOf(channel.toUpperCase())); }
            catch (Exception ignored) { PlayerStates.setChannel(PlayerStates.ChatChannel.UNKNOWN); }
    }
}
