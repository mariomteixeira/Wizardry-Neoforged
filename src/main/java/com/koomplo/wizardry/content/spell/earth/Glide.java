package com.koomplo.wizardry.content.spell.earth;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.event.EBLivingTick;
import com.koomplo.wizardry.api.content.item.ICastItem;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Glide extends Spell {

    public static final SpellProperty<Float> SPEED = SpellProperty.floatProperty("speed");
    public static final SpellProperty<Float> FALL_SPEED = SpellProperty.floatProperty("fall_speed");
    public static final SpellProperty<Float> ACCELERATION = SpellProperty.floatProperty("acceleration");

    @Override
    public boolean cast(PlayerCastContext ctx) {
        var caster = ctx.caster();

        if (caster.getDeltaMovement().y < -0.1 && !caster.isInWater()) {
            float speed = property(SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY);
            // The vanilla slow-down produces a 'terminal velocity', so potency must scale acceleration too
            float acceleration = property(ACCELERATION) * ctx.modifiers().get(SpellModifiers.POTENCY);

            Vec3 motion = caster.getDeltaMovement();
            caster.setDeltaMovement(motion.x, -property(FALL_SPEED), motion.z);

            if (Math.abs(motion.x) < speed && Math.abs(motion.z) < speed) {
                caster.addDeltaMovement(new Vec3(caster.getLookAngle().x * acceleration, 0, caster.getLookAngle().z * acceleration));
            }

            caster.fallDistance = 0.0f;
        }

        if (ctx.world().isClientSide) {
            for (int i = 0; i < 2; i++) {
                double x = caster.getX() - 0.25 + ctx.world().random.nextDouble() / 2;
                double y = caster.getY() + ctx.world().random.nextDouble();
                double z = caster.getZ() - 0.25 + ctx.world().random.nextDouble() / 2;
                ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).velocity(0, -0.1, 0)
                        .time(15).color(1f, 1f, 1f).spawn(ctx.world());
            }
        }

        return true;
    }

    @Override
    public boolean isInstantCast() {
        return false;
    }

    /** True while the entity is mid-cast of a flight-stance spell (glide or flight). */
    public static boolean isCastingFlightSpell(LivingEntity entity) {
        if (!entity.isUsingItem()) return false;
        ItemStack stack = entity.getUseItem();
        if (!(stack.getItem() instanceof ICastItem castItem)) return false;
        Spell spell = castItem.getCurrentSpell(stack);
        return spell instanceof Glide || spell instanceof Flight;
    }

    /**
     * Holds the vanilla elytra pose (hitbox + eye height) while gliding/flying, via the NeoForge forced pose.
     * Runs on both sides: the server syncs the pose, the caster's client predicts it.
     */
    public static void onLivingTick(EBLivingTick event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isCastingFlightSpell(player)) {
            if (player.getForcedPose() != Pose.FALL_FLYING) player.setForcedPose(Pose.FALL_FLYING);
        } else if (player.getForcedPose() == Pose.FALL_FLYING) {
            player.setForcedPose(null);
        }
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
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.EARTH, SpellType.UTILITY, SpellAction.FLYING, 5, 0, 0)
                .add(SPEED, 0.4f)
                .add(FALL_SPEED, 0.1f)
                .add(ACCELERATION, 0.1f)
                .build();
    }
}
