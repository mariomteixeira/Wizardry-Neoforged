package com.koomplo.wizardry.api.content.util;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;

/** A direction relative to an entity's horizontal facing (1.12.2 util, used by divination's chat readout). */
public enum RelativeFacing {

    DOWN("down", -1),
    UP("up", -1),
    FRONT("front", 0),
    BACK("back", 2),
    LEFT("left", 3),
    RIGHT("right", 1);

    public final String name;
    private final int horizontalIndex;

    private static final RelativeFacing[] HORIZONTALS = new RelativeFacing[4];

    RelativeFacing(String name, int horizontalIndex) {
        this.name = name;
        this.horizontalIndex = horizontalIndex;
    }

    static {
        for (RelativeFacing facing : values()) {
            if (facing.horizontalIndex > -1) HORIZONTALS[facing.horizontalIndex] = facing;
        }
    }

    public static RelativeFacing relativise(Direction absolute, Entity relativeTo) {
        if (absolute == Direction.DOWN) return DOWN;
        if (absolute == Direction.UP) return UP;
        Direction look = relativeTo.getDirection();
        int relativeIndex = absolute.get2DDataValue() - look.get2DDataValue();
        if (relativeIndex < 0) relativeIndex += 4;
        return HORIZONTALS[relativeIndex];
    }
}
