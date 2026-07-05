package com.koomplo.wizardry.content.spell.ice;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.content.block.StatueBlock;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class IceStatue extends RaySpell {

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (entityHit.getEntity() instanceof Mob target && !ctx.world().isClientSide) {
            ((StatueBlock) EBBlocks.ICE_STATUE.get()).convertToStatue(target, ctx.caster(),
                    (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)));
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
        float brightness = 0.5f + ctx.world().random.nextFloat() * 0.5f;
        ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).time(12 + ctx.world().random.nextInt(8))
                .color(brightness, brightness + 0.1f, 1.0f).spawn(ctx.world());
        ParticleBuilder.create(EBParticles.SNOW).pos(x, y, z).time(20 + ctx.world().random.nextInt(10)).spawn(ctx.world());
    }

    @Override
    protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration) {
        // 1.12.2 uses suffixed sounds for this spell
        SoundEvent sound = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                getLocation().getNamespace(), "spell." + getLocation().getPath() + ".shoot"));
        world.playSound(null, x, y, z, sound, SoundSource.PLAYERS, getVolume(),
                getPitch() + getPitchVariation() * (world.random.nextFloat() - 0.5f));
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.ICE, SpellType.ATTACK, SpellAction.POINT, 15, 0, 40)
                .add(DefaultProperties.RANGE, 10f)
                .add(DefaultProperties.EFFECT_DURATION, 400)
                .build();
    }
}
