package com.binaris.wizardry.capabilities;

import com.binaris.wizardry.api.content.data.ConjureData;
import com.binaris.wizardry.setup.registries.EBAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Tracks conjure state directly on the {@code ItemStack}. ItemStack is not a NeoForge
 * {@link net.neoforged.neoforge.attachment.IAttachmentHolder} in 1.21 (see {@link EBAttachments}), so this
 * stores its state in the {@link EBAttachments#CONJURE_DATA} data component on the stack instead of an
 * attachment.
 */
public class ConjureDataHolder implements ConjureData {
    private final ItemStack stack;

    private ConjureDataHolder(ItemStack stack) {
        this.stack = stack;
    }

    public static ConjureDataHolder get(ItemStack stack) {
        return new ConjureDataHolder(stack);
    }

    private CompoundTag tag() {
        CompoundTag tag = stack.get(EBAttachments.CONJURE_DATA.get());
        return tag != null ? tag : new CompoundTag();
    }

    private void save(CompoundTag tag) {
        stack.set(EBAttachments.CONJURE_DATA.get(), tag);
    }

    @Override
    public long getExpireTime() {
        CompoundTag tag = tag();
        if (!tag.contains("expire_time")) {
            tag.putLong("expire_time", -1L);
            save(tag);
        }
        return tag.getLong("expire_time");
    }

    @Override
    public void setExpireTime(long expireTime) {
        CompoundTag tag = tag();
        tag.putLong("expire_time", expireTime);
        save(tag);
    }

    @Override
    public int getDuration() {
        CompoundTag tag = tag();
        if (!tag.contains("duration")) {
            tag.putInt("duration", 0);
            save(tag);
        }
        return tag.getInt("duration");
    }

    @Override
    public void setDuration(int duration) {
        CompoundTag tag = tag();
        tag.putInt("duration", duration);
        save(tag);
    }

    @Override
    public boolean isSummoned() {
        CompoundTag tag = tag();
        if (!tag.contains("is_summoned")) {
            tag.putBoolean("is_summoned", false);
            save(tag);
        }
        return tag.getBoolean("is_summoned");
    }

    @Override
    public void setSummoned(boolean summoned) {
        CompoundTag tag = tag();
        tag.putBoolean("is_summoned", summoned);
        save(tag);
    }
}
