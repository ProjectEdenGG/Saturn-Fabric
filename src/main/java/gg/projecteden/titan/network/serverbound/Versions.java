package gg.projecteden.titan.network.serverbound;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.models.PluginMessage;
import gg.projecteden.titan.network.models.Serverbound;
import gg.projecteden.titan.saturn.Saturn;

public class Versions extends Serverbound {

    String titan;
    String saturn;

    public Versions() {
        this.titan = Titan.version();
        this.saturn = Saturn.version();
    }

    @Override
    public PluginMessage getType() {
        return PluginMessage.VERSIONS;
    }
}
