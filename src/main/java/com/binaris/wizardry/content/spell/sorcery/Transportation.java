package com.binaris.wizardry.content.spell.sorcery;

import com.binaris.wizardry.api.content.event.EBLivingTick;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.capabilities.WizardDataHolder;
import com.binaris.wizardry.content.block.TransportationStoneBlock;
import com.binaris.wizardry.core.integrations.ArtifactChannel;
import com.binaris.wizardry.core.networking.s2c.TransportationS2C;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.EBItems;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Transportation extends Spell {

    public static final SpellProperty<Integer> TELEPORT_COUNTDOWN = SpellProperty.intProperty("teleport_countdown");

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        Player caster = ctx.caster();
        Level world = ctx.world();
        WizardDataHolder data = caster.getData(EBAttachments.WIZARD_DATA);

        // Fixes the sound not playing in first person
        if (world.isClientSide) this.playSound(world, caster, ctx.castingTicks(), -1);

        if (data.getTransportationCountdown() > 0) return false; // Already teleporting

        BlockPos destination = data.getTransportationPos();
        String dimension = data.getTransportationDimension();

        if (destination == null) {
            if (!world.isClientSide) caster.displayClientMessage(Component.translatable("spell.ebwizardry.transportation.undefined"), true);
            return false;
        }

        // TODO charm_transportation (marco 6): 1.12.2 permite até 4 círculos lembrados + seleção por mira com o charm
        if (!world.dimension().location().toString().equals(dimension)) {
            if (!world.isClientSide) caster.displayClientMessage(Component.translatable("spell.ebwizardry.transportation.wrongdimension"), true);
            return false;
        }

        if (TransportationStoneBlock.testForCircle(world, destination)) {
            this.playSound(world, caster, 0, -1);
            if (!world.isClientSide) {
                caster.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 150, 0));
                data.setTransportationCountdown(property(TELEPORT_COUNTDOWN));
            }
            return true;
        } else {
            if (!world.isClientSide) caster.displayClientMessage(Component.translatable("spell.ebwizardry.transportation.missing"), true);
            return false;
        }
    }

    /** Server-side countdown; teleports the player to the remembered circle when it hits 1 (1.12.2 ticker). */
    public static void onLivingTick(EBLivingTick event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) return;

        WizardDataHolder data = player.getData(EBAttachments.WIZARD_DATA);
        int countdown = data.getTransportationCountdown();
        if (countdown <= 0) return;

        BlockPos destination = data.getTransportationPos();
        String dimension = data.getTransportationDimension();

        if (countdown == 1 && destination != null
                && player.level().dimension().location().toString().equals(dimension)) {

            Entity mount = player.getVehicle();
            if (mount != null) player.stopRiding();

            player.teleportTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);

            boolean teleportMount = mount != null && ArtifactChannel.isEquipped(player, EBItems.CHARM_MOUNT_TELEPORTING.get());

            if (teleportMount) {
                mount.teleportTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);
                player.startRiding(mount);
            }

            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));

            Services.NETWORK_HELPER.sendToDimension(player.getServer(),
                    new TransportationS2C(destination, teleportMount ? -1 : player.getId()),
                    player.level().dimension());
        }

        data.setTransportationCountdown(countdown - 1);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.SORCERY, SpellType.UTILITY, SpellAction.POINT_UP, 100, 20, 100)
                .add(TELEPORT_COUNTDOWN, 75)
                .build();
    }
}
