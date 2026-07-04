package com.binaris.wizardry.content.entity.construct;

import com.binaris.wizardry.api.content.entity.construct.MagicConstructEntity;
import com.binaris.wizardry.api.content.util.BlockUtil;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.earth.Earthquake;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.EBEntities;
import com.binaris.wizardry.setup.registries.Spells;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** NOT a scaled construct — the ring size is controlled by time (1.12.2 EntityEarthquake). */
public class EarthquakeConstruct extends MagicConstructEntity {

    public EarthquakeConstruct(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EarthquakeConstruct(Level world) {
        super(EBEntities.EARTHQUAKE.get(), world);
    }

    @Override
    public void tick() {
        super.tick();

        double speed = Spells.EARTHQUAKE.property(Earthquake.SPREAD_SPEED);

        if (!level().isClientSide && getCaster() != null && EntityUtil.canDamageBlocks(getCaster(), level())) {

            // The further the earthquake spreads, the finer the angle increments
            for (float angle = 0; angle < 2 * Math.PI; angle += Math.PI / (lifetime * 1.5)) {

                int x = this.getX() < 0 ? (int) (this.getX() + ((this.tickCount * speed) + 1.5) * Mth.sin(angle) - 1)
                        : (int) (this.getX() + ((this.tickCount * speed) + 1.5) * Mth.sin(angle));
                int y = (int) (this.getY() - 0.5);
                int z = this.getZ() < 0 ? (int) (this.getZ() + ((this.tickCount * speed) + 1.5) * Mth.cos(angle) - 1)
                        : (int) (this.getZ() + ((this.tickCount * speed) + 1.5) * Mth.cos(angle));

                BlockPos pos = new BlockPos(x, y, z);

                boolean canBreak = getCaster() instanceof ServerPlayer player
                        ? BlockUtil.canBreak(player, level(), pos, true)
                        : !(getCaster() instanceof net.minecraft.world.entity.Mob mob) || BlockUtil.canBreak(mob, level(), pos);

                if (!BlockUtil.isBlockUnbreakable(level(), pos) && !level().isEmptyBlock(pos)
                        && level().getBlockState(pos).isRedstoneConductor(level(), pos)
                        // The block above must not be solid, since that causes the falling block to vanish
                        && !level().getBlockState(pos.above()).isRedstoneConductor(level(), pos.above())
                        && canBreak) {

                    FallingBlockEntity fallingblock = FallingBlockEntity.fall(level(), pos, level().getBlockState(pos));
                    fallingblock.setDeltaMovement(0, 0.3, 0);
                    // fall() already spawned the entity with zero velocity; without this the hop never
                    // reaches clients and the wave reads as a static flash
                    fallingblock.hurtMarked = true;
                }
            }
        }

        List<LivingEntity> targets = EntityUtil.getLivingWithinRadius(
                (this.tickCount * speed) + 1.5, this.getX(), this.getY(), this.getZ(), level());

        // The caster is always in the centre, hence completely unaffected
        targets.remove(this.getCaster());

        for (LivingEntity target : targets) {
            // Searches in a 1-wide ring
            if (this.distanceTo(target) > (this.tickCount * speed) + 0.5
                    && target.getY() < this.getY() + 1 && target.getY() > this.getY() - 1) {

                // Knockback must be removed here, or the target falls into the floor
                Vec3 motion = target.getDeltaMovement();

                if (this.isValidTarget(target)) {
                    target.hurt(MagicDamageSource.causeIndirectMagicDamage(this, this.getCaster(), EBDamageSources.BLAST),
                            10 * this.damageMultiplier);
                    if (!level().isClientSide) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
                }

                // All targets are thrown, even damage-immune ones, so they don't sink into the ground
                target.setDeltaMovement(motion.x, 0.8, motion.z);

                if (target instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
                }
            }
        }
    }
}
