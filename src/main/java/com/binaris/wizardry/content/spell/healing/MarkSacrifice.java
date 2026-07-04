package com.binaris.wizardry.content.spell.healing;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.event.EBLivingHurtEvent;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.BuffSpell;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.EBMobEffects;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MarkSacrifice extends RaySpell {

    private static final float DAMAGE_INCREASE_PER_LEVEL = 0.6f;

    public MarkSacrifice() {
        this.soundValues(1, 1.1f, 0.2f);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (entityHit.getEntity() instanceof LivingEntity target && !ctx.world().isClientSide) {
            target.addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.MARK_OF_SACRIFICE),
                    (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)),
                    property(DefaultProperties.EFFECT_STRENGTH) + BuffSpell.getStandardBonusAmplifier(ctx.modifiers().get(SpellModifiers.POTENCY))));
        }
        return true;
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
        ParticleBuilder.create(EBParticles.DARK_MAGIC).pos(x, y, z).color(0xe9, 0x0e, 0x48).spawn(ctx.world());
        ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).time(12 + ctx.world().random.nextInt(8)).color(0xff, 0x7b, 0xbb).spawn(ctx.world());
    }

    // The next magic damage dealt to a marked creature is amplified and consumes the mark (1.12.2 parity)
    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.isCanceled()) return;

        MobEffectInstance effect = event.getDamagedEntity().getEffect(EBMobEffects.holder(EBMobEffects.MARK_OF_SACRIFICE));
        if (effect != null && EBDamageSources.isMagic(event.getSource())) {
            event.setAmount(event.getAmount() * (1 + (1 + effect.getAmplifier()) * DAMAGE_INCREASE_PER_LEVEL));
            if (!event.getDamagedEntity().level().isClientSide) {
                event.getDamagedEntity().removeEffect(EBMobEffects.holder(EBMobEffects.MARK_OF_SACRIFICE));
            }
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.HEALING, SpellType.ALTERATION, SpellAction.POINT, 25, 0, 60)
                .add(DefaultProperties.RANGE, 8f)
                .add(DefaultProperties.EFFECT_DURATION, 200)
                .add(DefaultProperties.EFFECT_STRENGTH, 0)
                .build();
    }
}
