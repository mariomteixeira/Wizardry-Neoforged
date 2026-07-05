package com.binaris.wizardry.content.spell.sorcery;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.content.block.StatueBlock;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBBlocks;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Petrify extends RaySpell {

    // This is more descriptive and more accurate than the standard "effect_duration" in this case
    public static final SpellProperty<Integer> MINIMUM_EFFECT_DURATION = SpellProperty.intProperty("minimum_effect_duration");

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (entityHit.getEntity() instanceof Mob target && !ctx.world().isClientSide) {
            ((StatueBlock) EBBlocks.PETRIFIED_STONE.get()).convertToStatue(target, ctx.caster(),
                    (int) (property(MINIMUM_EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)));
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
        ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).time(12 + ctx.world().random.nextInt(8))
                .color(0.2f, 0.2f, 0.2f).spawn(ctx.world());
        ParticleBuilder.create(EBParticles.DARK_MAGIC).pos(x, y, z).color(0.1f, 0.1f, 0.1f).spawn(ctx.world());
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.SORCERY, SpellType.ATTACK, SpellAction.POINT, 40, 20, 100)
                .add(DefaultProperties.RANGE, 10f)
                .add(MINIMUM_EFFECT_DURATION, 900)
                .build();
    }
}
