package gg.projecteden.titan.network.serverbound;

import gg.projecteden.titan.config.Config;
import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.models.PluginMessage;
import gg.projecteden.titan.network.models.Serverbound;

public class TitanConfig extends Serverbound {

    @Override
    public String getJson() {
        return ServerClientMessaging.GSON.toJson(Config.getJsonObject(Config.CONFIG_FILE));
    }

    @Override
    public PluginMessage getType() {
        return PluginMessage.TITAN_CONFIG;
    }
}
