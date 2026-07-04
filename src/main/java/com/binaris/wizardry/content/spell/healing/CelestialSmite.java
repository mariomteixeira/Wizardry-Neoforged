package com.binaris.wizardry.content.spell.healing;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.GeometryUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CelestialSmite extends RaySpell {

    public CelestialSmite() {
        this.ignoreLivingEntities(true);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        Vec3 hit = blockHit.getLocation();

        double radius = property(DefaultProperties.EFFECT_RADIUS) * ctx.modifiers().get(SpellModifiers.BLAST);

        List<LivingEntity> targets = EntityUtil.getLivingWithinRadius(radius, hit.x, hit.y, hit.z, ctx.world());

        DamageSource source = ctx.caster() == null ? ctx.world().damageSources().magic()
                : MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.RADIANT);
        float damage = property(DefaultProperties.DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY);

        for (LivingEntity target : targets) {
            EntityUtil.attackEntityWithoutKnockback(target, source, damage);
            target.igniteForSeconds(property(DefaultProperties.EFFECT_DURATION));
        }

        if (ctx.world().isClientSide) {
            ParticleBuilder.create(EBParticles.BEAM).pos(hit.x, ctx.world().getMaxBuildHeight(), hit.z).target(hit)
                    .scale(8).color(0xff, 0xbf, 0x00).time(10).spawn(ctx.world());
            ParticleBuilder.create(EBParticles.SPHERE).pos(hit).scale(4).color(0xff, 0xf0, 0x98).spawn(ctx.world());

            if (blockHit.getDirection() == Direction.UP) {
                Vec3 vec = hit.add(Vec3.atLowerCornerOf(blockHit.getDirection().getNormal()).scale(GeometryUtil.ANTI_Z_FIGHTING_OFFSET));
                ParticleBuilder.create(EBParticles.SCORCH).pos(vec).face(blockHit.getDirection()).scale(3).spawn(ctx.world());
            }
        }
        return true;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.HEALING, SpellType.ATTACK, SpellAction.POINT, 100, 25, 200)
                .add(DefaultProperties.RANGE, 40f)
                .add(DefaultProperties.EFFECT_RADIUS, 3)
                .add(DefaultProperties.DAMAGE, 8f)
                .add(DefaultProperties.EFFECT_DURATION, 10)
                .build();
    }
}
