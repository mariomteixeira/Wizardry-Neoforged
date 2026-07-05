package com.koomplo.wizardry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** Client-side registry of who is possessing what, used by the player renderer to swap in the creature's body. */
@OnlyIn(Dist.CLIENT)
public final class PossessionClientHandler {

    /** Display creatures keyed by possessing player entity id. */
    private static final Map<Integer, Mob> POSSESSEES = new HashMap<>();

    private PossessionClientHandler() {
    }

    public static void startDisplay(int playerId, String entityTypeId, CompoundTag nbt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        ResourceLocation id = ResourceLocation.tryParse(entityTypeId);
        if (id == null) return;

        Entity entity = BuiltInRegistries.ENTITY_TYPE.get(id).create(minecraft.level);
        if (entity instanceof Mob mob) {
            mob.load(nbt);
            mob.hurtTime = 0;
            POSSESSEES.put(playerId, mob);
        }
    }

    public static void endDisplay(int playerId) {
        POSSESSEES.remove(playerId);
    }

    @Nullable
    public static Mob getDisplayPossessee(int playerId) {
        return POSSESSEES.get(playerId);
    }

    public static void clear() {
        POSSESSEES.clear();
    }
}
