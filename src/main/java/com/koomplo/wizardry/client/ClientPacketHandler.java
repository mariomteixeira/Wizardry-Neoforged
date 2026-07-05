package com.koomplo.wizardry.client;

import com.koomplo.wizardry.core.networking.ClientMessageHandler;
import com.koomplo.wizardry.core.networking.s2c.ConfigSyncS2C;
import com.koomplo.wizardry.core.networking.s2c.NPCSpellCastS2C;
import com.koomplo.wizardry.core.networking.s2c.ParticleBuilderS2C;
import com.koomplo.wizardry.core.networking.s2c.ScreenShakeS2C;
import com.koomplo.wizardry.core.networking.s2c.SpellCastS2C;
import com.koomplo.wizardry.core.networking.s2c.SpellGlyphPacketS2C;
import com.koomplo.wizardry.core.networking.s2c.SpellPropertiesSyncS2C;
import com.koomplo.wizardry.core.networking.s2c.TestParticlePacketS2C;
import com.koomplo.wizardry.network.ArcaneLockSyncPacketS2C;
import com.koomplo.wizardry.network.ClientMessageHandlerForge;
import com.koomplo.wizardry.network.ContainmentSyncPacketS2C;
import com.koomplo.wizardry.network.MinionSyncPacketS2C;
import com.koomplo.wizardry.network.PlayerCapabilitySyncPacketS2C;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Handles messages received on the client side, we may only call client-side methods from here because we don't want to
 * accidentally reference client-only code on the server side.
 * <p>
 * Each S2C payload's {@code handle} method calls into this class from within {@code IPayloadContext.enqueueWork},
 * which only ever runs on the receiving (client) side for client-bound payloads.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleScreenShake(ScreenShakeS2C packet) {
        ScreenShakeHandler.triggerScreenShake(packet.intensity(), packet.duration());
    }

    /** Spawns the clairvoyance guiding trail along the received path (1.12.2 Clairvoyance#spawnPathPaticles). */
    public static void handleClairvoyance(com.koomplo.wizardry.core.networking.s2c.ClairvoyanceS2C packet) {
        var world = net.minecraft.client.Minecraft.getInstance().level;
        if (world == null || packet.points().isEmpty()) return;

        float duration = com.koomplo.wizardry.setup.registries.Spells.CLAIRVOYANCE
                .property(com.koomplo.wizardry.content.spell.DefaultProperties.DURATION);
        var points = packet.points();
        int interval = com.koomplo.wizardry.content.spell.sorcery.Clairvoyance.PARTICLE_MOVEMENT_INTERVAL;

        for (int i = 0; i < points.size() - 1; i += 2) {
            var point = points.get(i);
            var nextPoint = points.size() - i <= 2 ? points.get(points.size() - 1) : points.get(Math.min(i + 2, points.size() - 1));

            com.koomplo.wizardry.api.client.ParticleBuilder.create(com.koomplo.wizardry.setup.registries.client.EBParticles.PATH)
                    .pos(point.getX() + 0.5, point.getY() + 0.5, point.getZ() + 0.5)
                    .velocity((nextPoint.getX() - point.getX()) / (float) interval,
                            (nextPoint.getY() - point.getY()) / (float) interval,
                            (nextPoint.getZ() - point.getZ()) / (float) interval)
                    .time((int) (duration * packet.durationMultiplier())).color(0f, 1f, 0.3f).spawn(world);
        }

        var end = points.get(points.size() - 1);
        com.koomplo.wizardry.api.client.ParticleBuilder.create(com.koomplo.wizardry.setup.registries.client.EBParticles.PATH)
                .pos(end.getX() + 0.5, end.getY() + 0.5, end.getZ() + 0.5)
                .time((int) (duration * packet.durationMultiplier())).color(1f, 1f, 1f).spawn(world);
    }

    /** Starts/stops rendering a possessing player as the possessed creature (1.12.2 PacketPossession). */
    public static void handlePossession(com.koomplo.wizardry.core.networking.s2c.PossessionS2C packet) {
        if (packet.entityTypeId().isEmpty()) {
            PossessionClientHandler.endDisplay(packet.playerId());
        } else {
            PossessionClientHandler.startDisplay(packet.playerId(), packet.entityTypeId(), packet.entityNbt());
        }
    }

    /** Revives the client-side copy of a resurrected player and closes the death screen if it's us (1.12.2 PacketResurrection). */
    public static void handleResurrection(com.koomplo.wizardry.core.networking.s2c.ResurrectionS2C packet) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        net.minecraft.world.entity.player.Player player;

        if (packet.entityId() == minecraft.player.getId()) {
            player = minecraft.player;
            minecraft.setScreen(null); // Off the death screen and back into the fight
        } else {
            var entity = minecraft.level.getEntity(packet.entityId());
            if (!(entity instanceof net.minecraft.world.entity.player.Player otherPlayer)) return;
            player = otherPlayer;
        }

        player.revive();
        ((com.koomplo.wizardry.core.mixin.accessor.LivingEntityDeadAccessor) player).EBWIZARDRY$setDead(false);
        player.deathTime = 0;
        player.setHealth(player.getMaxHealth() / 2);

        com.koomplo.wizardry.api.client.ParticleBuilder.spawnHealParticles(minecraft.level, player);

        var sound = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                com.koomplo.wizardry.WizardryMainMod.location("spell.resurrection"));
        minecraft.level.playLocalSound(player.getX(), player.getY(), player.getZ(), sound,
                net.minecraft.sounds.SoundSource.PLAYERS, 1, 1, false);
    }

    /** Arrival effects of the transportation spell: dismount, travel sound and particle rings (1.12.2 ClientProxy). */
    public static void handleTransportation(com.koomplo.wizardry.core.networking.s2c.TransportationS2C packet) {
        var world = net.minecraft.client.Minecraft.getInstance().level;
        if (world == null) return;

        var pos = packet.destination();

        if (packet.dismountEntityId() != -1) {
            var entity = world.getEntity(packet.dismountEntityId());
            if (entity != null) entity.stopRiding();
        }

        // Played on receipt rather than on send so it is heard in first person (1.12.2 fix)
        var travelSound = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                com.koomplo.wizardry.WizardryMainMod.location("spell.transportation.travel"));
        world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), travelSound,
                net.minecraft.sounds.SoundSource.PLAYERS, 1, 1, false);

        for (int i = 0; i < 20; i++) {
            double radius = 1;
            float angle = world.random.nextFloat() * (float) Math.PI * 2;
            double x = pos.getX() + 0.5 + radius * net.minecraft.util.Mth.cos(angle);
            double y = pos.getY() + world.random.nextDouble() * 2;
            double z = pos.getZ() + 0.5 + radius * net.minecraft.util.Mth.sin(angle);
            com.koomplo.wizardry.api.client.ParticleBuilder.create(com.koomplo.wizardry.setup.registries.client.EBParticles.SPARKLE)
                    .pos(x, y, z).velocity(0, 0.02, 0).color(0.6f, 1f, 0.6f)
                    .time(80 + world.random.nextInt(10)).spawn(world);
        }
        for (int i = 0; i < 20; i++) {
            double radius = 1;
            float angle = world.random.nextFloat() * (float) Math.PI * 2;
            double x = pos.getX() + 0.5 + radius * net.minecraft.util.Mth.cos(angle);
            double y = pos.getY() + world.random.nextDouble() * 2;
            double z = pos.getZ() + 0.5 + radius * net.minecraft.util.Mth.sin(angle);
            world.addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0.02, 0);
        }
        for (int i = 0; i < 20; i++) {
            double radius = 1;
            float angle = world.random.nextFloat() * (float) Math.PI * 2;
            double x = pos.getX() + 0.5 + radius * net.minecraft.util.Mth.cos(angle);
            double y = pos.getY() + world.random.nextDouble() * 2;
            double z = pos.getZ() + 0.5 + radius * net.minecraft.util.Mth.sin(angle);
            world.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, x, y, z, 0, 0.02, 0);
        }
    }

    public static void handleTestParticle(TestParticlePacketS2C packet) {
        ClientMessageHandler.testParticle(packet);
    }

    public static void handleSpellGlyph(SpellGlyphPacketS2C packet) {
        ClientMessageHandler.spellGlyph(packet);
    }

    public static void handleSpellCast(SpellCastS2C packet) {
        ClientMessageHandler.spellCast(packet);
    }

    public static void handleNPCSpellCast(NPCSpellCastS2C packet) {
        ClientMessageHandler.npcSpellCast(packet);
    }

    public static void handleConfigSync(ConfigSyncS2C packet) {
        ClientMessageHandler.configSync(packet);
    }

    public static void handleSpellPropertiesSync(SpellPropertiesSyncS2C packet) {
        ClientMessageHandler.spellPropertiesSync(packet);
    }

    public static void handleParticleBuilder(ParticleBuilderS2C packet) {
        ClientMessageHandler.particleBuilder(packet);
    }

    public static void handleMinionSync(MinionSyncPacketS2C packet) {
        ClientMessageHandlerForge.minionSync(packet);
    }

    public static void handleContainmentSync(ContainmentSyncPacketS2C packet) {
        ClientMessageHandlerForge.containmentSync(packet);
    }

    public static void handleArcaneLock(ArcaneLockSyncPacketS2C packet) {
        ClientMessageHandlerForge.arcaneLock(packet);
    }

    public static void handlePlayerCapabilitySync(PlayerCapabilitySyncPacketS2C packet) {
        ClientMessageHandlerForge.playerCapabilitySync(packet);
    }
}
