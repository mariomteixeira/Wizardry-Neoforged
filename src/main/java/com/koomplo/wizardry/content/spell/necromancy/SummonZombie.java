package com.koomplo.wizardry.content.spell.necromancy;

import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.MinionSpell;
import com.koomplo.wizardry.core.integrations.ArtifactChannel;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SummonZombie extends MinionSpell<Zombie> {
    public SummonZombie() {
        super(Zombie::new);
        this.soundValues(7, 0.6f, 0);
    }

    @Override
    protected Zombie createMinion(Level world, @Nullable LivingEntity caster, SpellModifiers modifiers) {
        if (caster instanceof Player player && ArtifactChannel.isEquipped(player, EBItems.CHARM_MINION_VARIANTS.get())) {
            return new Husk(EntityType.HUSK, world);
        } else {
            return super.createMinion(world, caster, modifiers);
        }
    }

    @Override
    protected void addMinionExtras(Zombie minion, CastContext ctx, int alreadySpawned) {
        if (ctx.caster() instanceof Player player && ArtifactChannel.isEquipped(player, EBItems.CHARM_UNDEAD_HELMETS.get())) {
            minion.setItemSlot(EquipmentSlot.HEAD, Items.LEATHER_HELMET.getDefaultInstance());
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.NOVICE, Elements.NECROMANCY, SpellType.MINION, SpellAction.SUMMON, 10, 0, 40)
                .add(DefaultProperties.MINION_LIFETIME, 600)
                .add(DefaultProperties.MINION_COUNT, 1)
                .add(DefaultProperties.SUMMON_RADIUS, 2)
                .build();
    }
}
