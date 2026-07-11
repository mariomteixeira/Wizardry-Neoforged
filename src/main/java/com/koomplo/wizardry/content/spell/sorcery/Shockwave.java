package com.koomplo.wizardry.content.spell.sorcery;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.AreaEffectSpell;
import com.koomplo.wizardry.core.config.EBServerConfig;
import com.koomplo.wizardry.core.integrations.ArtifactChannel;
import com.koomplo.wizardry.core.networking.s2c.ScreenShakeS2C;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Shockwave extends AreaEffectSpell {

    public static final SpellProperty<Float> MAX_REPULSION_VELOCITY = SpellProperty.floatProperty("max_repulsion_velocity");
    /** The radius within which maximum damage is dealt and maximum repulsion velocity is applied. */
    private static final double EPICENTRE_RADIUS = 1;

    public Shockwave() {
        super();
        this.soundValues(2, 0.5f, 0);
        this.alwaysSucceed(true);
    }

    @Override
    protected boolean affectEntity(CastContext ctx, Vec3 origin, LivingEntity target, int targetCount) {
        float radius = property(DefaultProperties.EFFECT_RADIUS) * ctx.modifiers().get(SpellModifiers.BLAST);

        if (target instanceof Player targetPlayer) {
            if (!EBServerConfig.PLAYERS_MOVE_EACH_OTHER.get()) return false;
            if (ArtifactChannel.isEquipped(targetPlayer, EBItems.AMULET_ANCHORING.get())) {
                if (!ctx.world().isClientSide && ctx instanceof PlayerCastContext playerCtx) {
                    playerCtx.caster().displayClientMessage(
                            Component.translatable("spell.resist", target.getName(), this.getDescriptionFormatted()), true);
                }
                return false;
            }
        }

        // Linear profile: 0 at the edge, 1 at (and within) the epicentre radius
        float proximity = (float) (1 - (Math.max(origin.distanceTo(target.position()) - EPICENTRE_RADIUS, 0)) / (radius - EPICENTRE_RADIUS));

        target.hurt(ctx.caster() != null ? MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.BLAST)
                        : target.damageSources().magic(),
                property(DefaultProperties.DAMAGE) * proximity * ctx.modifiers().get(SpellModifiers.POTENCY));

        if (!ctx.world().isClientSide) {
            double velocityFactor = proximity * property(MAX_REPULSION_VELOCITY);

            double dx = target.getX() - origin.x;
            double dy = target.getY() + 1 - origin.y;
            double dz = target.getZ() - origin.z;

            target.setDeltaMovement(velocityFactor * dx, velocityFactor * dy, velocityFactor * dz);

            // Player motion is handled on that player's client so needs packets
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
            }
        }
        return true;
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        boolean result = super.cast(ctx);
        if (result && !ctx.world().isClientSide) {
            double radius = property(DefaultProperties.EFFECT_RADIUS) * ctx.modifiers().get(SpellModifiers.BLAST);
            // 1.12.2 shook every nearby player's screen (shakiness 10); intensity mapped to the port's handler
            EntityUtil.getEntitiesWithinRadius(radius, ctx.caster().getX(), ctx.caster().getY(), ctx.caster().getZ(),
                    ctx.world(), ServerPlayer.class).forEach(p -> Services.NETWORK_HELPER.sendTo(p, new ScreenShakeS2C(10f)));
        }
        return result;
    }

    @Override
    protected void spawnParticleEffect(CastContext ctx, Vec3 origin, double radius) {
        Level world = ctx.world();

        for (int i = 0; i < 40; i++) {
            double particleX = origin.x - 1.0d + 2 * world.random.nextDouble();
            double particleZ = origin.z - 1.0d + 2 * world.random.nextDouble();

            BlockState block = world.getBlockState(BlockPos.containing(origin.x, origin.y - 0.5, origin.z));
            if (!block.isAir()) {
                world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block),
                        particleX, origin.y, particleZ, particleX - origin.x, 0, particleZ - origin.z);
            }
        }

        ParticleBuilder.create(EBParticles.SPHERE).pos(origin.add(0, 0.1, 0)).scale((float) radius * 0.8f)
                .color(0.8f, 0.9f, 1f).spawn(world);
        world.addParticle(ParticleTypes.EXPLOSION_EMITTER, origin.x, origin.y + 0.1, origin.z, 0, 0, 0);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.SORCERY, SpellType.ATTACK, SpellAction.POINT_DOWN, 65, 20, 150)
                .add(DefaultProperties.EFFECT_RADIUS, 5)
                .add(DefaultProperties.DAMAGE, 8f)
                .add(MAX_REPULSION_VELOCITY, 3f)
                .build();
    }
}
