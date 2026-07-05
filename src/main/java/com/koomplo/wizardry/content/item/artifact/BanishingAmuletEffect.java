package com.koomplo.wizardry.content.item.artifact;

import com.koomplo.wizardry.content.spell.necromancy.Banish;
import com.koomplo.wizardry.core.IArtifactEffect;
import com.koomplo.wizardry.setup.registries.Spells;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

public class BanishingAmuletEffect implements IArtifactEffect {
    @Override
    public void onPlayerHurt(Player player, DamageSource source, AtomicDouble amount, AtomicBoolean canceled, ItemStack artifact) {
        if (player.level().random.nextFloat() < 0.2f && !(source.getEntity() != source.getDirectEntity()) && source.getEntity() instanceof LivingEntity sourceEntity) {
            ((Banish) Spells.BANISH).teleport(sourceEntity, player.level(), 8 + player.level().random.nextDouble() * 8);
        }
    }
}