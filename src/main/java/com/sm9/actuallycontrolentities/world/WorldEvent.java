package com.sm9.actuallycontrolentities.world;

import com.sm9.actuallycontrolentities.spawner.ACESpawnEntry;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public class WorldEvent extends Event {
    @Cancelable
    public static class PotentialSpawns extends WorldEvent {
        private final List<ACESpawnEntry> list;

        public PotentialSpawns(List<ACESpawnEntry> oldList) {
            if (oldList != null) {
                this.list = new ArrayList<>(oldList);
            } else {
                this.list = new ArrayList<>();
            }
        }

        public List<ACESpawnEntry> getList() {
            return list;
        }
    }
}