package gg.projecteden.titan.network.clientbound;

import gg.projecteden.titan.discord.RichPresence;
import gg.projecteden.titan.network.models.Clientbound;

public class ResetMinigame extends Clientbound {

    @Override
    public void onReceive() {
        RichPresence.updateDetails(null);
        RichPresence.resetTimestamp();
    }
}
