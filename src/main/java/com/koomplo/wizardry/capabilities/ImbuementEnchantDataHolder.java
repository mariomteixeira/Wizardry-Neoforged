package com.koomplo.wizardry.capabilities;

import com.koomplo.wizardry.api.content.data.ImbuementEnchantData;
import com.koomplo.wizardry.setup.registries.EBAttachments;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks temporary imbuement enchantments directly on the {@code ItemStack}. ItemStack is not a NeoForge
 * {@link net.neoforged.neoforge.attachment.IAttachmentHolder} in 1.21 (see {@link EBAttachments}), so this
 * stores its state in the {@link EBAttachments#IMBUEMENT_ENCHANT_DATA} data component on the stack instead of
 * an attachment.
 */
public class ImbuementEnchantDataHolder implements ImbuementEnchantData {
    private final ItemStack stack;

    private ImbuementEnchantDataHolder(ItemStack stack) {
        this.stack = stack;
    }

    public static ImbuementEnchantDataHolder get(ItemStack stack) {
        return new ImbuementEnchantDataHolder(stack);
    }

    private CompoundTag tag() {
        CompoundTag tag = stack.get(EBAttachments.IMBUEMENT_ENCHANT_DATA.get());
        return tag != null ? tag : new CompoundTag();
    }

    private void save(CompoundTag tag) {
        stack.set(EBAttachments.IMBUEMENT_ENCHANT_DATA.get(), tag);
    }

    private static ResourceLocation keyOf(Holder<Enchantment> enchant) {
        return enchant.unwrapKey().map(ResourceKey::location).orElse(null);
    }

    @Override
    public void addImbuement(Holder<Enchantment> enchant, long expireTime) {
        ResourceLocation enchantKey = keyOf(enchant);
        if (enchantKey == null) return;
        String enchantId = enchantKey.toString();

        CompoundTag tag = tag();
        if (!tag.contains(enchantId)) {
            tag.putLong(enchantId, expireTime);
            save(tag);
        }
    }

    @Override
    public Map<ResourceLocation, Long> getImbuements() {
        Map<ResourceLocation, Long> result = new HashMap<>();
        CompoundTag tag = tag();

        tag.getAllKeys().forEach(key -> {
            try {
                ResourceLocation enchantId = ResourceLocation.tryParse(key);
                long expireTime = tag.getLong(key);
                result.put(enchantId, expireTime);
            } catch (Exception e) {
                // Ignore invalid keys
            }
        });

        return result;
    }

    @Override
    public void removeImbuement(Holder<Enchantment> enchant) {
        ResourceLocation enchantKey = keyOf(enchant);
        if (enchantKey == null) return;
        String enchantId = enchantKey.toString();

        CompoundTag tag = tag();
        if (tag.contains(enchantId)) {
            tag.remove(enchantId);
            save(tag);
        }
    }

    @Override
    public boolean isImbuement(Holder<Enchantment> enchant) {
        ResourceLocation enchantId = keyOf(enchant);
        if (enchantId == null) return false;
        return tag().contains(enchantId.toString());
    }

    @Override
    public long getExpirationTime(Holder<Enchantment> enchantment) {
        ResourceLocation enchantId = keyOf(enchantment);
        if (enchantId == null) return -1;
        CompoundTag tag = tag();
        if (tag.contains(enchantId.toString()))
            return tag.getLong(enchantId.toString());
        return -1;
    }
}
