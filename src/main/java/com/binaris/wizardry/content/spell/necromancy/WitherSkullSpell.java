package com.binaris.wizardry.content.spell.necromancy;

import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.EntityCastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WitherSkullSpell extends Spell {
    @Override
    public boolean cast(PlayerCastContext ctx) {
        Vec3 look = ctx.caster().getLookAngle();
        double acceleration = property(DefaultProperties.SPEED) * ctx.modifiers().get(SpellModifiers.RANGE);
        WitherSkull witherSkull = new WitherSkull(ctx.world(), ctx.caster(), look.scale(acceleration));
        witherSkull.setPos(ctx.caster().getX() + look.x, ctx.caster().getY() + look.y + 1.3, ctx.caster().getZ() + look.z);
        witherSkull.accelerationPower = acceleration;
        witherSkull.setDeltaMovement(look.scale(acceleration));

        witherSkull.setOwner(ctx.caster());
        ctx.world().addFreshEntity(witherSkull);

        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    public boolean cast(EntityCastContext ctx) {
        Vec3 look = ctx.caster().getLookAngle();
        double acceleration = property(DefaultProperties.SPEED);
        WitherSkull witherSkull = new WitherSkull(ctx.world(), ctx.caster(), look.scale(acceleration));
        witherSkull.setPos(ctx.caster().getX() + look.x, ctx.caster().getY() + look.y + 1.3, ctx.caster().getZ() + look.z);
        witherSkull.accelerationPower = acceleration;
        witherSkull.setDeltaMovement(look.scale(acceleration));

        witherSkull.setOwner(ctx.caster());
        ctx.world().addFreshEntity(witherSkull);

        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.NECROMANCY, SpellType.ATTACK, SpellAction.POINT, 20, 5, 30)
                .add(DefaultProperties.SPEED, 0.1F)
                .build();
    }
}
