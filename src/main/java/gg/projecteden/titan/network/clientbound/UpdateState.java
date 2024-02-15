package gg.projecteden.titan.network.clientbound;

import gg.projecteden.titan.network.models.Clientbound;

public class UpdateState extends Clientbound {

    String mode;
    String worldGroup;
    String arena;
    String mechanic;

    @Override
    public void onReceive() {
    }
}
