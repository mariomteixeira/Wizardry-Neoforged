package com.koomplo.wizardry.content.spell.sorcery;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.api.content.util.RelativeFacing;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import com.koomplo.wizardry.core.config.EBServerConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Divination extends Spell {

    private static final float NUDGE_SPEED = 0.2f;

    /** The different 'signal strengths' for the divination spell (chat readouts and effects). */
    protected enum Strength {

        NOTHING("nothing", -1),
        WEAK("weak", 0),
        MODERATE("moderate", 0.25f),
        STRONG("strong", 0.5f),
        VERY_STRONG("very_strong", 0.75f);

        final String key;
        final float minWeight;

        Strength(String key, float minWeight) {
            this.key = key;
            this.minWeight = minWeight;
        }

        protected static Strength forWeight(float weight) {
            return Arrays.stream(values()).filter(s -> s.minWeight < weight).max(Comparator.naturalOrder()).orElse(NOTHING);
        }
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        double range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

        List<BlockPos> sphere = BlockUtil.getBlockSphere(ctx.caster().blockPosition(), range);

        sphere.removeIf(b -> {
            BlockState state = ctx.world().getBlockState(b);
            return !(state.is(Tags.Blocks.ORES) || state.is(EBBlocks.CRYSTAL_ORE.get()) || state.is(EBBlocks.DEEPSLATE_CRYSTAL_ORE.get())
                    || EBServerConfig.DIVINATION_ORE_WHITELIST.get().contains(
                            net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock())));
        });

        Strength strength = Strength.NOTHING;
        Direction direction = Direction.DOWN; // Doesn't matter what this is

        if (!sphere.isEmpty()) {
            sphere.sort(Comparator.comparingDouble(b -> calculateWeight(ctx.world(), ctx.caster(), b, range, ctx.modifiers())));

            BlockPos target = sphere.get(sphere.size() - 1);

            direction = Direction.getNearest((float) (target.getX() + 0.5 - ctx.caster().getX()),
                    (float) (target.getY() + 0.5 - (ctx.caster().getY() + ctx.caster().getEyeHeight())),
                    (float) (target.getZ() + 0.5 - ctx.caster().getZ()));

            strength = Strength.forWeight(calculateWeight(ctx.world(), ctx.caster(), target, range, ctx.modifiers()));
        }

        if (!ctx.world().isClientSide) {
            ctx.caster().displayClientMessage(Component.translatable("spell.ebwizardry.divination." + strength.key,
                    Component.translatable("spell.ebwizardry.divination." + RelativeFacing.relativise(direction, ctx.caster()).name)), false);
        } else {
            switch (strength) {
                case NOTHING, WEAK -> { }
                case MODERATE -> spawnHintParticles(ctx.world(), ctx.caster(), 3, direction);
                case STRONG -> spawnHintParticles(ctx.world(), ctx.caster(), 8, direction);
                case VERY_STRONG -> {
                    spawnHintParticles(ctx.world(), ctx.caster(), 12, direction);
                    ctx.caster().addDeltaMovement(new Vec3(direction.getStepX() * NUDGE_SPEED,
                            direction.getStepY() * NUDGE_SPEED, direction.getStepZ() * NUDGE_SPEED));
                }
            }
        }

        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    private static void spawnHintParticles(Level world, Entity caster, int count, Direction direction) {
        Vec3 vec = Vec3.atCenterOf(caster.blockPosition().above().relative(direction, 2));

        for (int i = 0; i < count; i++) {
            ParticleBuilder.create(EBParticles.FLASH, world.random, vec.x, vec.y, vec.z, 0.7, false)
                    .time(20 + world.random.nextInt(5)).color(0.6f + world.random.nextFloat() * 0.4f,
                            0.6f + world.random.nextFloat() * 0.4f, 0.6f + world.random.nextFloat() * 0.4f)
                    .scale(0.3f).spawn(world);
        }
    }

    protected static float calculateWeight(Level world, Player caster, BlockPos pos, double range, com.koomplo.wizardry.api.content.spell.internal.SpellModifiers modifiers) {
        BlockState state = world.getBlockState(pos);
        // On a non-sorcery wand, the value of the ore has no effect on its weight
        float weightModifier = modifiers.get(SpellModifiers.POTENCY) - 1;

        // xp is a decent way of determining the 'value' of a block (some randomness is expected, as in 1.12.2)
        float xp = world instanceof ServerLevel serverLevel
                ? state.getExpDrop(serverLevel, pos, null, caster, ItemStack.EMPTY) : 0;
        // Smelting gives a lot less than mining, hence the 4x
        if (xp == 0) {
            xp = 4 * world.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(new ItemStack(state.getBlock())), world)
                    .map(r -> r.value().getExperience()).orElse(0f);
        }

        double distance = Math.sqrt(caster.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        return (float) (1 - distance / range + 0.2 * weightModifier * xp);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.SORCERY, SpellType.UTILITY, SpellAction.THRUST, 35, 0, 80)
                .add(DefaultProperties.RANGE, 8f)
                .build();
    }
}
