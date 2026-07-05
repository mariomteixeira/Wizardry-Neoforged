package com.koomplo.wizardry.content.spell.lightning;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.EntityCastContext;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.core.AllyDesignation;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Thunderstorm extends Spell {

    public static final SpellProperty<Integer> LIGHTNING_BOLTS = SpellProperty.intProperty("lightning_bolts");
    public static final SpellProperty<Float> SECONDARY_DAMAGE = SpellProperty.floatProperty("secondary_damage");
    public static final SpellProperty<Float> TERTIARY_DAMAGE = SpellProperty.floatProperty("tertiary_damage");
    public static final SpellProperty<Float> SECONDARY_RANGE = SpellProperty.floatProperty("secondary_range");
    public static final SpellProperty<Float> TERTIARY_RANGE = SpellProperty.floatProperty("tertiary_range");
    /** This is per secondary target. */
    public static final SpellProperty<Integer> TERTIARY_MAX_TARGETS = SpellProperty.intProperty("tertiary_max_targets");

    private static final float CENTRE_RADIUS_FRACTION = 0.5f;

    @Override
    public boolean cast(PlayerCastContext ctx) {
        return doCasting(ctx.world(), ctx.caster(), ctx.modifiers());
    }

    @Override
    public boolean cast(EntityCastContext ctx) {
        return doCasting(ctx.world(), ctx.caster(), ctx.modifiers());
    }

    @Override
    public boolean canCastByEntity() {
        return true;
    }

    // This spell is exactly the same for players and NPCs
    private boolean doCasting(Level world, LivingEntity caster, SpellModifiers modifiers) {

        if (!world.canSeeSky(caster.blockPosition().above())) return false;

        double maxRadius = property(DefaultProperties.EFFECT_RADIUS);

        for (int i = 0; i < property(LIGHTNING_BOLTS); i++) {

            double radius = maxRadius * CENTRE_RADIUS_FRACTION + world.random.nextDouble() * maxRadius
                    * (1 - CENTRE_RADIUS_FRACTION) * modifiers.get(SpellModifiers.BLAST);
            float angle = world.random.nextFloat() * (float) Math.PI * 2;

            double x = caster.getX() + radius * Mth.cos(angle);
            double z = caster.getZ() + radius * Mth.sin(angle);
            Integer y = BlockUtil.getNearestFloor(world, BlockPos.containing(x, caster.getY(), z), (int) maxRadius);

            if (y == null) continue;

            if (!world.isClientSide) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
                if (lightning != null) {
                    lightning.moveTo(x, y, z);
                    // Attribution keys shared with the lightning_bolt spell (EntityStruckByLightningEvent handler)
                    lightning.getPersistentData().putUUID(LightningBoltSpell.SUMMONER_NBT_KEY, caster.getUUID());
                    lightning.getPersistentData().putFloat(LightningBoltSpell.DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));
                    world.addFreshEntity(lightning);
                }
            }

            // Secondary chaining effect
            List<LivingEntity> secondaryTargets = EntityUtil.getLivingWithinRadius(
                    property(SECONDARY_RANGE), x, y + 1, z, world);

            for (int j = 0; j < Math.min(secondaryTargets.size(), property(DefaultProperties.SECONDARY_MAX_TARGETS)); j++) {

                LivingEntity secondaryTarget = secondaryTargets.get(j);

                if (AllyDesignation.isValidTarget(caster, secondaryTarget)) {

                    if (world.isClientSide) {
                        ParticleBuilder.create(EBParticles.LIGHTNING).pos(x, y, z).target(secondaryTarget).spawn(world);
                        ParticleBuilder.spawnShockParticles(world, secondaryTarget.getX(),
                                secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2, secondaryTarget.getZ());
                    }

                    playSound(world, secondaryTarget, 0, -1);

                    secondaryTarget.hurt(MagicDamageSource.causeDirectMagicDamage(caster, EBDamageSources.SHOCK),
                            property(SECONDARY_DAMAGE) * modifiers.get(SpellModifiers.POTENCY));

                    // Tertiary chaining effect
                    List<LivingEntity> tertiaryTargets = EntityUtil.getLivingWithinRadius(
                            property(TERTIARY_RANGE), secondaryTarget.getX(),
                            secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2, secondaryTarget.getZ(), world);

                    for (int k = 0; k < Math.min(tertiaryTargets.size(), property(TERTIARY_MAX_TARGETS)); k++) {

                        LivingEntity tertiaryTarget = tertiaryTargets.get(k);

                        if (!secondaryTargets.contains(tertiaryTarget)
                                && AllyDesignation.isValidTarget(caster, tertiaryTarget)) {

                            if (world.isClientSide) {
                                ParticleBuilder.create(EBParticles.LIGHTNING).entity(secondaryTarget)
                                        .pos(0, secondaryTarget.getBbHeight() / 2, 0).target(tertiaryTarget).spawn(world);
                                ParticleBuilder.spawnShockParticles(world, tertiaryTarget.getX(),
                                        tertiaryTarget.getY() + tertiaryTarget.getBbHeight() / 2, tertiaryTarget.getZ());
                            }

                            playSound(world, tertiaryTarget, 0, -1);

                            tertiaryTarget.hurt(MagicDamageSource.causeDirectMagicDamage(caster, EBDamageSources.SHOCK),
                                    property(TERTIARY_DAMAGE) * modifiers.get(SpellModifiers.POTENCY));
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.LIGHTNING, SpellType.ATTACK, SpellAction.POINT_UP, 100, 20, 250)
                .add(DefaultProperties.EFFECT_RADIUS, 10)
                .add(LIGHTNING_BOLTS, 10)
                .add(SECONDARY_RANGE, 10f)
                .add(DefaultProperties.SECONDARY_MAX_TARGETS, 12)
                .add(SECONDARY_DAMAGE, 10f)
                .add(TERTIARY_RANGE, 10f)
                .add(TERTIARY_MAX_TARGETS, 3)
                .add(TERTIARY_DAMAGE, 8f)
                .build();
    }
}
