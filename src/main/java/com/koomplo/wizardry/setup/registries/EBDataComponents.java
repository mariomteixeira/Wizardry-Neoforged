package com.koomplo.wizardry.setup.registries;

import com.koomplo.wizardry.WizardryMainMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for the mod's custom {@link DataComponentType}s. In 1.20.5+ item data that used to live in the
 * {@code ItemStack} NBT compound is stored as strongly-typed data components instead. Each type below replaces a
 * former NBT key (see {@code CastItemDataHelper} / {@code RegistryUtils} for the old key names).
 * <p>
 * Components built with only {@code .persistent(codec)} still synchronise to the client: {@code Builder#build()}
 * derives a network {@code StreamCodec} from the persistent codec when none is supplied.
 */
public final class EBDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, WizardryMainMod.MOD_ID);

    /** The list of spells stored on a cast item, as a list of registry ids. Replaces NBT key {@code "spells"}. */
    public static final Supplier<DataComponentType<List<ResourceLocation>>> SPELLS = COMPONENTS.register("spells",
            () -> DataComponentType.<List<ResourceLocation>>builder()
                    .persistent(ResourceLocation.CODEC.listOf())
                    .build());

    /** The index of the currently selected spell. Replaces NBT key {@code "selectedSpell"}. */
    public static final Supplier<DataComponentType<Integer>> SELECTED_SPELL = COMPONENTS.register("selected_spell",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    /** Per-slot game-time at which each spell's cooldown ends. Replaces NBT key {@code "cooldownEndTime"}. */
    public static final Supplier<DataComponentType<List<Long>>> COOLDOWN_END_TIMES = COMPONENTS.register("cooldown_end_times",
            () -> DataComponentType.<List<Long>>builder()
                    .persistent(Codec.LONG.listOf())
                    .build());

    /** Per-slot maximum cooldown (in ticks). Replaces NBT key {@code "maxCooldown"}. */
    public static final Supplier<DataComponentType<List<Integer>>> MAX_COOLDOWNS = COMPONENTS.register("max_cooldowns",
            () -> DataComponentType.<List<Integer>>builder()
                    .persistent(Codec.INT.listOf())
                    .build());

    /** Applied wand upgrades, mapping the upgrade's NBT key to its level. Replaces NBT key {@code "upgrades"}. */
    public static final Supplier<DataComponentType<Map<String, Integer>>> UPGRADES = COMPONENTS.register("upgrades",
            () -> DataComponentType.<Map<String, Integer>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
                    .build());

    /** The item's progression level. Replaces NBT key {@code "progression"}. */
    public static final Supplier<DataComponentType<Integer>> PROGRESSION = COMPONENTS.register("progression",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    /** The single spell stored on a spell book, as a registry id. Replaces NBT key {@code "Spell"}. */
    public static final Supplier<DataComponentType<ResourceLocation>> SPELL = COMPONENTS.register("spell",
            () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .build());

    /** The spell tier stored on an arcane tome, as a registry id. Replaces NBT key {@code "Tier"}. */
    public static final Supplier<DataComponentType<ResourceLocation>> TIER = COMPONENTS.register("tier",
            () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .build());

    /** Remaining shots on a summoned flamecatcher. Replaces NBT key {@code "ShotsLeft"}. */
    public static final Supplier<DataComponentType<Integer>> SHOTS_LEFT = COMPONENTS.register("shots_left",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    private EBDataComponents() {
    }
}
