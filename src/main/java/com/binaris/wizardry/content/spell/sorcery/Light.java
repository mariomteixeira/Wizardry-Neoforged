package com.binaris.wizardry.content.spell.sorcery;

import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.content.blockentity.MagicLightBlockEntity;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.core.integrations.ArtifactChannel;
import com.binaris.wizardry.setup.registries.EBBlocks;
import com.binaris.wizardry.setup.registries.EBItems;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Light extends Spell {

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        double range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

        Vec3 origin = ctx.caster().getEyePosition();
        Vec3 endpoint = origin.add(ctx.caster().getLookAngle().scale(range));
        BlockHitResult rayTrace = ctx.world().clip(new ClipContext(origin, endpoint,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ctx.caster()));

        BlockPos pos;
        if (rayTrace.getType() == HitResult.Type.BLOCK) {
            pos = rayTrace.getBlockPos().relative(rayTrace.getDirection());
        } else {
            // No block in range: place the light in the air at max range (1.12.2 behaviour)
            pos = BlockPos.containing(endpoint);
        }

        if (!ctx.world().isEmptyBlock(pos)) return false;

        if (!ctx.world().isClientSide) {
            ctx.world().setBlockAndUpdate(pos, EBBlocks.MAGIC_LIGHT.get().defaultBlockState());
            if (ctx.world().getBlockEntity(pos) instanceof MagicLightBlockEntity light) {
                int lifetime = ArtifactChannel.isEquipped(ctx.caster(), EBItems.CHARM_LIGHT.get()) ? -1
                        : (int) (property(DefaultProperties.DURATION) * ctx.modifiers().get(SpellModifiers.DURATION));
                light.setLifetime(lifetime);
            }
        }

        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.NOVICE, Elements.SORCERY, SpellType.UTILITY, SpellAction.POINT, 5, 0, 15)
                .add(DefaultProperties.RANGE, 4f)
                .add(DefaultProperties.DURATION, 600)
                .build();
    }
}
