package com.binaris.wizardry.client;

import com.binaris.wizardry.core.networking.ClientMessageHandler;
import com.binaris.wizardry.core.networking.s2c.ConfigSyncS2C;
import com.binaris.wizardry.core.networking.s2c.NPCSpellCastS2C;
import com.binaris.wizardry.core.networking.s2c.ParticleBuilderS2C;
import com.binaris.wizardry.core.networking.s2c.ScreenShakeS2C;
import com.binaris.wizardry.core.networking.s2c.SpellCastS2C;
import com.binaris.wizardry.core.networking.s2c.SpellGlyphPacketS2C;
import com.binaris.wizardry.core.networking.s2c.SpellPropertiesSyncS2C;
import com.binaris.wizardry.core.networking.s2c.TestParticlePacketS2C;
import com.binaris.wizardry.network.ArcaneLockSyncPacketS2C;
import com.binaris.wizardry.network.ClientMessageHandlerForge;
import com.binaris.wizardry.network.ContainmentSyncPacketS2C;
import com.binaris.wizardry.network.MinionSyncPacketS2C;
import com.binaris.wizardry.network.PlayerCapabilitySyncPacketS2C;
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
    public static void handleClairvoyance(com.binaris.wizardry.core.networking.s2c.ClairvoyanceS2C packet) {
        var world = net.minecraft.client.Minecraft.getInstance().level;
        if (world == null || packet.points().isEmpty()) return;

        float duration = com.binaris.wizardry.setup.registries.Spells.CLAIRVOYANCE
                .property(com.binaris.wizardry.content.spell.DefaultProperties.DURATION);
        var points = packet.points();
        int interval = com.binaris.wizardry.content.spell.sorcery.Clairvoyance.PARTICLE_MOVEMENT_INTERVAL;

        for (int i = 0; i < points.size() - 1; i += 2) {
            var point = points.get(i);
            var nextPoint = points.size() - i <= 2 ? points.get(points.size() - 1) : points.get(Math.min(i + 2, points.size() - 1));

            com.binaris.wizardry.api.client.ParticleBuilder.create(com.binaris.wizardry.setup.registries.client.EBParticles.PATH)
                    .pos(point.getX() + 0.5, point.getY() + 0.5, point.getZ() + 0.5)
                    .velocity((nextPoint.getX() - point.getX()) / (float) interval,
                            (nextPoint.getY() - point.getY()) / (float) interval,
                            (nextPoint.getZ() - point.getZ()) / (float) interval)
                    .time((int) (duration * packet.durationMultiplier())).color(0f, 1f, 0.3f).spawn(world);
        }

        var end = points.get(points.size() - 1);
        com.binaris.wizardry.api.client.ParticleBuilder.create(com.binaris.wizardry.setup.registries.client.EBParticles.PATH)
                .pos(end.getX() + 0.5, end.getY() + 0.5, end.getZ() + 0.5)
                .time((int) (duration * packet.durationMultiplier())).color(1f, 1f, 1f).spawn(world);
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
