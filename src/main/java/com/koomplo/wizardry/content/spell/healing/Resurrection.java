package com.koomplo.wizardry.content.spell.healing;

import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.capabilities.WizardDataHolder;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.core.mixin.accessor.LivingEntityDeadAccessor;
import com.koomplo.wizardry.core.networking.s2c.ResurrectionS2C;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.registries.EBAttachments;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class Resurrection extends Spell {

    public static final SpellProperty<Integer> WAIT_TIME = SpellProperty.intProperty("wait_time");

    /** Game time of the player's death, stamped in persistent data (deathTime freezes when the corpse despawns). */
    public static final String DEATH_STAMP_NBT_KEY = "ebwizardryDeathGameTime";

    @Override
    public boolean requiresPacket() {
        return false; // Has its own packets
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        Player caster = ctx.caster();
        Level world = ctx.world();
        WizardDataHolder data = caster.getData(EBAttachments.WIZARD_DATA);

        double radius = property(DefaultProperties.EFFECT_RADIUS) * ctx.modifiers().get(SpellModifiers.RANGE);

        if (!world.isClientSide && caster.getServer() != null) {
            // Potency reduces the time you have to wait to resurrect an ally
            int waitTime = (int) (property(WAIT_TIME) / ctx.modifiers().get(SpellModifiers.POTENCY));

            ServerPlayer nearestDeadAlly = caster.getServer().getPlayerList().getPlayers().stream()
                    .filter(p -> !p.isAlive() && p.level() == world
                            && p.getPersistentData().getLong(DEATH_STAMP_NBT_KEY) > 0
                            && world.getGameTime() - p.getPersistentData().getLong(DEATH_STAMP_NBT_KEY) > waitTime
                            && (data.isPlayerAlly(p) || caster == p)
                            && p.distanceToSqr(caster) < radius * radius)
                    .min(Comparator.comparingDouble(caster::distanceToSqr))
                    .orElse(null);

            if (nearestDeadAlly != null) {

                resurrect(nearestDeadAlly);

                // Notify clients to reset the appropriate fields, spawn particles and play sounds
                Services.NETWORK_HELPER.sendToDimension(caster.getServer(),
                        new ResurrectionS2C(nearestDeadAlly.getId()), world.dimension());

                if (caster == nearestDeadAlly) {
                    caster.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                            "spell.ebwizardry.resurrection.resurrect_self", caster.getDisplayName()), false);
                } else {
                    caster.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                            "spell.ebwizardry.resurrection.resurrect_ally", nearestDeadAlly.getDisplayName(), caster.getDisplayName()), false);
                }

                this.playSound(world, caster, ctx.castingTicks(), -1);
                return true;
            }
        }

        return false;
    }

    /** Brings the given player back to life with half health, re-adding them to the world. */
    public static void resurrect(ServerPlayer player) {
        player.revive(); // Clears the removal reason (the corpse entity is removed 20 ticks after death)
        ((LivingEntityDeadAccessor) player).EBWIZARDRY$setDead(false);
        player.setHealth(player.getMaxHealth() / 2);
        player.deathTime = 0;
        // Experience doesn't normally get reset until respawn, so we need to do that here too
        player.experienceLevel = 0;
        player.totalExperience = 0;
        player.experienceProgress = 0;
        player.getPersistentData().remove(DEATH_STAMP_NBT_KEY);
        player.serverLevel().addRespawnedPlayer(player);
    }

    /** Stamps the death game time; hooked from LivingDeathEvent on the game bus. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putLong(DEATH_STAMP_NBT_KEY, player.level().getGameTime());
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.HEALING, SpellType.ALTERATION, SpellAction.SUMMON, 150, 25, 400)
                .add(DefaultProperties.EFFECT_RADIUS, 8)
                .add(WAIT_TIME, 200)
                .build();
    }
}
