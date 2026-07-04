package com.binaris.wizardry.content.spell.earth;

import com.binaris.wizardry.api.content.entity.construct.MagicConstructEntity;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.content.entity.construct.EarthquakeConstruct;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.ConstructSpell;
import com.binaris.wizardry.core.networking.s2c.ScreenShakeS2C;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Earthquake extends ConstructSpell<EarthquakeConstruct> {

    public static final SpellProperty<Float> SPREAD_SPEED = SpellProperty.floatProperty("spread_speed");

    public Earthquake() {
        super(EarthquakeConstruct::new, true);
        this.soundValues(2, 1, 0);
        this.overlap(true);
        this.floor(true);
    }

    @Override
    public boolean requiresPacket() {
        return true;
    }

    @Override
    protected void addConstructExtras(CastContext ctx, EarthquakeConstruct construct, @Nullable Direction side) {
        // Lifetime derives from base radius and spread speed, overwriting the -1 from permanent=true
        construct.lifetime = (int) (property(DefaultProperties.EFFECT_RADIUS) / property(SPREAD_SPEED)
                * ctx.modifiers().get(SpellModifiers.BLAST));
    }

    @Override
    protected boolean spawnConstruct(CastContext ctx, Vec3 vec3, @Nullable Direction side) {
        if (ctx.world().isClientSide) {
            double x = vec3.x, y = vec3.y, z = vec3.z;

            ctx.world().addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y + 0.1, z, 0, 0, 0);

            BlockState block = ctx.world().getBlockState(BlockPos.containing(x, y - 0.5, z));
            if (!block.isAir()) {
                for (int i = 0; i < 40; i++) {
                    double particleX = x - 1.0d + 2 * ctx.world().random.nextDouble();
                    double particleZ = z - 1.0d + 2 * ctx.world().random.nextDouble();
                    ctx.world().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block),
                            particleX, y, particleZ, particleX - x, 0, particleZ - z);
                }
            }
        } else {
            EntityUtil.getEntitiesWithinRadius(15, vec3.x, vec3.y, vec3.z, ctx.world(), ServerPlayer.class)
                    .forEach(p -> Services.NETWORK_HELPER.sendTo(p, new ScreenShakeS2C(2.5f, 18)));
        }

        return super.spawnConstruct(ctx, vec3, side);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.EARTH, SpellType.ATTACK, SpellAction.POINT_DOWN, 75, 25, 250)
                .add(DefaultProperties.EFFECT_RADIUS, 8)
                .add(SPREAD_SPEED, 0.4f)
                .add(DefaultProperties.DURATION, 600)
                .build();
    }
}
