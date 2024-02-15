package gg.projecteden.titan.network.serverbound;

import gg.projecteden.titan.network.models.PluginMessage;
import gg.projecteden.titan.network.models.Serverbound;

public class Handshake extends Serverbound {

    String messagingVersion = "1.0";

    @Override
    public PluginMessage getType() {
        return PluginMessage.HANDSHAKE;
    }
}
