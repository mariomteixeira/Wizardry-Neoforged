package com.koomplo.wizardry.network;

import com.koomplo.wizardry.core.networking.c2s.BlockUsePacketC2S;
import com.koomplo.wizardry.core.networking.c2s.ControlInputPacketC2S;
import com.koomplo.wizardry.core.networking.c2s.SpellAccessPacketC2S;
import com.koomplo.wizardry.core.networking.s2c.ConfigSyncS2C;
import com.koomplo.wizardry.core.networking.s2c.NPCSpellCastS2C;
import com.koomplo.wizardry.core.networking.s2c.ParticleBuilderS2C;
import com.koomplo.wizardry.core.networking.s2c.ScreenShakeS2C;
import com.koomplo.wizardry.core.networking.s2c.SpellCastS2C;
import com.koomplo.wizardry.core.networking.s2c.SpellGlyphPacketS2C;
import com.koomplo.wizardry.core.networking.s2c.SpellPropertiesSyncS2C;
import com.koomplo.wizardry.core.networking.s2c.TestParticlePacketS2C;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class EBForgeNetwork {
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1"); // versao do protocolo

        registrar.playToClient(ScreenShakeS2C.TYPE, ScreenShakeS2C.STREAM_CODEC, ScreenShakeS2C::handle);
        registrar.playToClient(com.koomplo.wizardry.core.networking.s2c.ClairvoyanceS2C.TYPE, com.koomplo.wizardry.core.networking.s2c.ClairvoyanceS2C.STREAM_CODEC, com.koomplo.wizardry.core.networking.s2c.ClairvoyanceS2C::handle);
        registrar.playToClient(com.koomplo.wizardry.core.networking.s2c.TransportationS2C.TYPE, com.koomplo.wizardry.core.networking.s2c.TransportationS2C.STREAM_CODEC, com.koomplo.wizardry.core.networking.s2c.TransportationS2C::handle);
        registrar.playToClient(com.koomplo.wizardry.core.networking.s2c.ResurrectionS2C.TYPE, com.koomplo.wizardry.core.networking.s2c.ResurrectionS2C.STREAM_CODEC, com.koomplo.wizardry.core.networking.s2c.ResurrectionS2C::handle);
        registrar.playToClient(TestParticlePacketS2C.TYPE, TestParticlePacketS2C.STREAM_CODEC, TestParticlePacketS2C::handle);
        registrar.playToServer(ControlInputPacketC2S.TYPE, ControlInputPacketC2S.STREAM_CODEC, ControlInputPacketC2S::handle);
        registrar.playToServer(BlockUsePacketC2S.TYPE, BlockUsePacketC2S.STREAM_CODEC, BlockUsePacketC2S::handle);
        registrar.playToServer(SpellAccessPacketC2S.TYPE, SpellAccessPacketC2S.STREAM_CODEC, SpellAccessPacketC2S::handle);
        registrar.playToClient(SpellGlyphPacketS2C.TYPE, SpellGlyphPacketS2C.STREAM_CODEC, SpellGlyphPacketS2C::handle);
        registrar.playToClient(SpellCastS2C.TYPE, SpellCastS2C.STREAM_CODEC, SpellCastS2C::handle);
        registrar.playToClient(NPCSpellCastS2C.TYPE, NPCSpellCastS2C.STREAM_CODEC, NPCSpellCastS2C::handle);
        registrar.playToClient(ConfigSyncS2C.TYPE, ConfigSyncS2C.STREAM_CODEC, ConfigSyncS2C::handle);
        registrar.playToClient(SpellPropertiesSyncS2C.TYPE, SpellPropertiesSyncS2C.STREAM_CODEC, SpellPropertiesSyncS2C::handle);
        registrar.playToClient(ParticleBuilderS2C.TYPE, ParticleBuilderS2C.STREAM_CODEC, ParticleBuilderS2C::handle);
        registrar.playToClient(MinionSyncPacketS2C.TYPE, MinionSyncPacketS2C.STREAM_CODEC, MinionSyncPacketS2C::handle);
        registrar.playToClient(ContainmentSyncPacketS2C.TYPE, ContainmentSyncPacketS2C.STREAM_CODEC, ContainmentSyncPacketS2C::handle);
        registrar.playToClient(ArcaneLockSyncPacketS2C.TYPE, ArcaneLockSyncPacketS2C.STREAM_CODEC, ArcaneLockSyncPacketS2C::handle);
        registrar.playToClient(PlayerCapabilitySyncPacketS2C.TYPE, PlayerCapabilitySyncPacketS2C.STREAM_CODEC, PlayerCapabilitySyncPacketS2C::handle);
    }

    private EBForgeNetwork() {
    }
}
