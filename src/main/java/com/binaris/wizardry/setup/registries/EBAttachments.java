package com.binaris.wizardry.setup.registries;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.capabilities.ArcaneLockDataHolder;
import com.binaris.wizardry.capabilities.CastCommandDataHolder;
import com.binaris.wizardry.capabilities.ContainmentDataHolder;
import com.binaris.wizardry.capabilities.MinionDataHolder;
import com.binaris.wizardry.capabilities.SpellManagerDataHolder;
import com.binaris.wizardry.capabilities.WizardDataHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Replaces the old Forge capability system for the 8 capability DataHolders. Entity/player/block-entity-scoped
 * holders register as {@link AttachmentType}s here. ItemStack is not an
 * {@link net.neoforged.neoforge.attachment.IAttachmentHolder} in NeoForge 1.21 (item persistent data lives in
 * data components instead), so the two item-scoped holders (ConjureDataHolder, ImbuementEnchantDataHolder)
 * register plain {@link DataComponentType}s on the mod's existing {@link EBDataComponents#COMPONENTS} register
 * instead.
 */
public final class EBAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WizardryMainMod.MOD_ID);

    public static final Supplier<AttachmentType<WizardDataHolder>> WIZARD_DATA =
            ATTACHMENTS.register("wizard_data", () -> AttachmentType
                    .serializable(holder -> new WizardDataHolder((Player) holder))
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<CastCommandDataHolder>> CAST_COMMAND_DATA =
            ATTACHMENTS.register("cast_command_data", () -> AttachmentType
                    .serializable(holder -> new CastCommandDataHolder((Player) holder))
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<SpellManagerDataHolder>> SPELL_MANAGER_DATA =
            ATTACHMENTS.register("spell_manager_data", () -> AttachmentType
                    .serializable(holder -> new SpellManagerDataHolder((Player) holder))
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<MinionDataHolder>> MINION_DATA =
            ATTACHMENTS.register("minion_data", () -> AttachmentType
                    .serializable(holder -> new MinionDataHolder((Mob) holder))
                    .build());

    public static final Supplier<AttachmentType<ContainmentDataHolder>> CONTAINMENT_DATA =
            ATTACHMENTS.register("containment_data", () -> AttachmentType
                    .serializable(holder -> new ContainmentDataHolder((LivingEntity) holder))
                    .build());

    public static final Supplier<AttachmentType<ArcaneLockDataHolder>> ARCANE_LOCK_DATA =
            ATTACHMENTS.register("arcane_lock_data", () -> AttachmentType
                    .serializable(holder -> new ArcaneLockDataHolder((BlockEntity) holder))
                    .build());

    public static final Supplier<DataComponentType<CompoundTag>> CONJURE_DATA =
            EBDataComponents.COMPONENTS.register("conjure_data", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .build());

    public static final Supplier<DataComponentType<CompoundTag>> IMBUEMENT_ENCHANT_DATA =
            EBDataComponents.COMPONENTS.register("imbuement_enchant_data", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .build());

    private EBAttachments() {
    }
}
