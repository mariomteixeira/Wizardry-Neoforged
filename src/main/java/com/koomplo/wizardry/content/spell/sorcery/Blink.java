package com.koomplo.wizardry.content.spell.sorcery;

import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.GeometryUtil;
import com.koomplo.wizardry.api.content.util.RayTracer;
import com.koomplo.wizardry.client.ScreenOverlays;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.core.integrations.ArtifactChannel;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Blink extends Spell {

    @Override
    public boolean cast(PlayerCastContext ctx) {
        var caster = ctx.caster();

        boolean teleportMount = caster.isPassenger() && ArtifactChannel.isEquipped(caster, EBItems.CHARM_MOUNT_TELEPORTING.get());
        boolean hitLiquids = teleportMount && caster.getVehicle() instanceof Boat; // Boats teleport to the surface

        double range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

        HitResult rayTrace = RayTracer.standardBlockRayTrace(ctx.world(), caster, range, hitLiquids, !hitLiquids, false);

        if (ctx.world().isClientSide) {
            for (int i = 0; i < 10; i++) {
                double dy = caster.getY() + 2 * ctx.world().random.nextFloat();
                // For portal particles, velocity is the offset where they start, drifting to the given position
                ctx.world().addParticle(ParticleTypes.PORTAL, caster.getX(), dy, caster.getZ(),
                        ctx.world().random.nextDouble() - 0.5,
                        ctx.world().random.nextDouble() - 0.5,
                        ctx.world().random.nextDouble() - 0.5);
            }
            ScreenOverlays.playBlinkEffect(caster);
        }

        if (rayTrace instanceof BlockHitResult blockHit && rayTrace.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            Entity toTeleport = teleportMount ? caster.getVehicle() : caster;
            if (toTeleport == null) return false;

            Vec3 vec = EntityUtil.findSpaceForTeleport(toTeleport, GeometryUtil.getFaceCentre(pos, Direction.DOWN), teleportMount);

            if (vec != null) {
                // Plays before and after so it is heard from both positions
                this.playSound(ctx.world(), caster, ctx.castingTicks(), -1);

                if (!teleportMount && caster.isPassenger()) caster.stopRiding();
                if (!ctx.world().isClientSide) toTeleport.teleportTo(vec.x, vec.y, vec.z);

                this.playSound(ctx.world(), caster, ctx.castingTicks(), -1);
                return true;
            }
        }
        return false;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.SORCERY, SpellType.UTILITY, SpellAction.POINT, 15, 0, 25)
                .add(DefaultProperties.RANGE, 25f)
                .build();
    }
}
