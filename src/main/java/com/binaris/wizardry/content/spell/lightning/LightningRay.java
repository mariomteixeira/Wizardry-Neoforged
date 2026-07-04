package com.binaris.wizardry.content.spell.lightning;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class LightningRay extends RaySpell {

    public LightningRay() {
        this.aimAssist(0.6f);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return true;

        if (MagicDamageSource.isEntityImmune(EBDamageSources.SHOCK, target)) {
            if (!ctx.world().isClientSide && ctx.castingTicks() == 1 && ctx instanceof PlayerCastContext playerCtx) {
                playerCtx.caster().displayClientMessage(
                        Component.translatable("spell.resist", target.getName(), this.getDescriptionFormatted()), true);
            }
        } else if (ctx.castingTicks() % 10 == 0) {
            DamageSource source = ctx.caster() != null ? MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.SHOCK)
                    : target.damageSources().magic();
            EntityUtil.attackEntityWithoutKnockback(target, source,
                    property(DefaultProperties.DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));
        }

        if (ctx.world().isClientSide) {
            if (ctx.castingTicks() % 3 == 0) {
                ParticleBuilder builder = ParticleBuilder.create(EBParticles.LIGHTNING);
                if (ctx.caster() != null) builder.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position()));
                else builder.pos(origin);
                builder.target(target).spawn(ctx.world());
            }
            for (int i = 0; i < 5; i++) {
                ParticleBuilder.create(EBParticles.SPARK, target).spawn(ctx.world());
            }
        }
        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        if (ctx.world().isClientSide && ctx.castingTicks() % 4 == 0) {
            // The arc does not reach full range when it has a free end
            double freeRange = 0.8 * property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

            if (ctx.caster() != null) {
                ParticleBuilder.create(EBParticles.LIGHTNING).entity(ctx.caster())
                        .pos(origin.subtract(ctx.caster().position())).length(freeRange).spawn(ctx.world());
            } else {
                ParticleBuilder.create(EBParticles.LIGHTNING).pos(origin)
                        .target(origin.add(direction.scale(freeRange))).spawn(ctx.world());
            }
        }
        return true;
    }

    @Override
    public boolean isInstantCast() {
        return false;
    }

    @Override
    protected void playSound(Level world, LivingEntity entity, int castTicks, int duration) {
        this.playSoundLoop(world, entity, castTicks);
    }

    @Override
    protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration) {
        this.playSoundLoop(world, x, y, z, ticksInUse, duration);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.LIGHTNING, SpellType.ATTACK, SpellAction.POINT, 5, 0, 0)
                .add(DefaultProperties.RANGE, 10f)
                .add(DefaultProperties.DAMAGE, 3f)
                .build();
    }
}
