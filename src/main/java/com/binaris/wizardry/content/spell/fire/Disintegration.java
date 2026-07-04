package com.binaris.wizardry.content.spell.fire;

import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.entity.projectile.EmberEntity;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import com.binaris.wizardry.api.client.ParticleBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Disintegration extends RaySpell {

    public static final SpellProperty<Integer> EMBER_COUNT = SpellProperty.intProperty("ember_count");
    public static final SpellProperty<Integer> EMBER_LIFETIME = SpellProperty.intProperty("ember_lifetime");

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        Entity target = entityHit.getEntity();

        if (MagicDamageSource.isEntityImmune(EBDamageSources.FIRE, target)) {
            if (!ctx.world().isClientSide && ctx instanceof PlayerCastContext playerCtx) {
                playerCtx.caster().displayClientMessage(
                        Component.translatable("spell.resist", target.getName(), this.getDescriptionFormatted()), true);
            }
        } else {
            target.igniteForSeconds((int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)));
            EntityUtil.attackEntityWithoutKnockback(target,
                    ctx.caster() == null ? ctx.world().damageSources().magic()
                            : MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.FIRE),
                    property(DefaultProperties.DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));

            if (target instanceof LivingEntity living && living.getHealth() <= 0) {
                spawnEmbers(ctx.world(), ctx.caster(), target, property(EMBER_COUNT));
            }
        }
        return true;
    }

    public static void spawnEmbers(Level world, @Nullable LivingEntity caster, Entity target, int count) {
        target.clearFire();

        if (world.isClientSide) return;

        for (int i = 0; i < count; i++) {
            EmberEntity ember = new EmberEntity(world, caster);
            double x = (world.random.nextDouble() - 0.5) * target.getBbWidth();
            double y = world.random.nextDouble() * target.getBbHeight();
            double z = (world.random.nextDouble() - 0.5) * target.getBbWidth();
            ember.setPos(target.getX() + x, target.getY() + y, target.getZ() + z);
            ember.tickCount = world.random.nextInt(20);
            float speed = 0.2f;
            ember.setDeltaMovement(x * speed, y * 0.5f * speed, z * speed);
            world.addFreshEntity(ember);
        }
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return true;
    }

    @Override
    protected void spawnParticle(CastContext ctx, double x, double y, double z, double vx, double vy, double vz) {
        ParticleBuilder.create(EBParticles.MAGIC_FIRE).pos(x, y, z).velocity(vx, vy, vz).scale(2)
                .time(10 + ctx.world().random.nextInt(4)).spawn(ctx.world());
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.FIRE, SpellType.ATTACK, SpellAction.POINT, 35, 0, 40)
                .add(DefaultProperties.RANGE, 10f)
                .add(DefaultProperties.DAMAGE, 8f)
                .add(DefaultProperties.EFFECT_DURATION, 10)
                .add(EMBER_LIFETIME, 300)
                .add(EMBER_COUNT, 12)
                .build();
    }
}
