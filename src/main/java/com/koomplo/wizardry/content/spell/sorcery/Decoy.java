package com.koomplo.wizardry.content.spell.sorcery;

import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.EntityCastContext;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.content.entity.living.DecoyEntity;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Decoy extends Spell {

    public static final SpellProperty<Integer> DECOY_LIFETIME = SpellProperty.intProperty("decoy_lifetime");
    public static final SpellProperty<Float> MOB_TRICK_CHANCE = SpellProperty.floatProperty("mob_trick_chance");

    @Override
    public boolean cast(PlayerCastContext ctx) {
        // Determines whether the caster moves left and the decoy moves right, or vice versa.
        // Uses the synchronised entity id to ensure it is consistent on client and server, but not always the same.
        double splitSpeed = ctx.caster().getId() % 2 == 0 ? 0.3 : -0.3;
        spawnDecoy(ctx.world(), ctx.caster(), ctx.modifiers(), splitSpeed);
        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    public boolean cast(EntityCastContext ctx) {
        double splitSpeed = ctx.world().random.nextBoolean() ? 0.3 : -0.3;
        spawnDecoy(ctx.world(), ctx.caster(), ctx.modifiers(), splitSpeed);
        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    public boolean canCastByEntity() {
        return true;
    }

    private void spawnDecoy(Level world, LivingEntity caster, SpellModifiers modifiers, double splitSpeed) {
        Vec3 look = caster.getLookAngle();

        if (!world.isClientSide) {
            DecoyEntity decoy = new DecoyEntity(world);
            decoy.setCaster(caster);
            decoy.lifetime = property(DECOY_LIFETIME);
            decoy.moveTo(caster.getX(), caster.getY(), caster.getZ(), caster.getYRot(), caster.getXRot());
            // Velocity before addFreshEntity, or the split never reaches clients
            decoy.setDeltaMovement(-look.z * splitSpeed, 0, look.x * splitSpeed);
            // A decoy of a player has its caster's name tag shown permanently, so the decoy can't be told apart
            // by the missing nameplate; non-player decoys show nothing
            if (caster instanceof Player) {
                decoy.setCustomName(Component.literal(caster.getName().getString()));
                decoy.setCustomNameVisible(true);
            }
            world.addFreshEntity(decoy);

            // Tricks any mobs that are targeting the caster into targeting the decoy instead.
            // More likely to trick mobs the higher the damage multiplier: the default base value is 0.5,
            // so modifiers of 2 or more will guarantee mobs are tricked
            for (Mob creature : EntityUtil.getEntitiesWithinRadius(16, caster.getX(), caster.getY(), caster.getZ(), world, Mob.class)) {
                if (creature.getTarget() == caster
                        && world.random.nextFloat() < property(MOB_TRICK_CHANCE) * modifiers.get(SpellModifiers.POTENCY)) {
                    creature.setTarget(decoy);
                }
            }
        }

        caster.push(look.z * splitSpeed, 0, -look.x * splitSpeed);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.SORCERY, SpellType.UTILITY, SpellAction.SUMMON, 40, 10, 200)
                .add(DECOY_LIFETIME, 600)
                .add(MOB_TRICK_CHANCE, 0.5f)
                .build();
    }
}
