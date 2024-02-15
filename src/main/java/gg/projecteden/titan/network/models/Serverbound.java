package gg.projecteden.titan.network.models;

import gg.projecteden.titan.network.ServerClientMessaging;

public abstract class Serverbound implements Message {

    public void onSend() {}

    public String getJson() {
        return ServerClientMessaging.GSON.toJson(this);
    }

    public abstract PluginMessage getType();
}
