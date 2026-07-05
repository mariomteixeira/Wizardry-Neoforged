package com.binaris.wizardry.content.blockentity;

import com.binaris.wizardry.content.block.StatueBlock;
import com.binaris.wizardry.setup.registries.EBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Stores the creature frozen inside a statue (1.12.2 TileEntityStatue). Only the bottom block holds the data. */
public class StatueBlockEntity extends BlockEntity {

    /** Full NBT of the stored creature. */
    private CompoundTag entityCompound = new CompoundTag();
    @Nullable
    private ResourceLocation entityName;
    private float entityYawHead;
    private float entityYawOffset;
    public float entityWidth = 1;
    public float entityHeight = 1;
    public boolean isIce;
    private int timer;
    private int lifetime = 600;

    /** The number of blocks this statue is made of. */
    public int parts;
    /** The position within the statue this particular block entity holds. 1 is at the bottom. */
    public int position = 1;

    /** Client-side display entity, lazily rebuilt from NBT by the renderer. */
    @Nullable
    public Mob displayCreature;

    public StatueBlockEntity(BlockPos pos, BlockState state) {
        super(EBBlockEntities.STATUE.get(), pos, state);
    }

    public StatueBlockEntity(BlockPos pos, BlockState state, boolean isIce) {
        this(pos, state);
        this.isIce = isIce;
    }

    public void setCreatureAndPart(Mob entity, int position, int parts) {
        this.position = position;
        this.parts = parts;
        this.entityWidth = entity.getBbWidth();
        this.entityHeight = entity.getBbHeight();

        if (position == 1) {
            // Aligns the entity with the block for visual effect when broken out
            entity.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY(), getBlockPos().getZ() + 0.5);
            this.entityCompound = new CompoundTag();
            entity.saveWithoutId(this.entityCompound);
            this.entityName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            this.entityYawHead = entity.yHeadRot;
            this.entityYawOffset = entity.yBodyRot;
        }

        setChanged();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    /** Releases the stored creature back into the world (called when the bottom block is broken). */
    public void releaseCreature() {
        if (level == null || level.isClientSide || entityName == null) return;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityName);
        Entity entity = type.create(level);

        if (entity instanceof Mob mob) {
            mob.load(entityCompound);
            mob.getPersistentData().remove(StatueBlock.PETRIFIED_NBT_KEY);
            mob.getPersistentData().remove(StatueBlock.FROZEN_NBT_KEY);
            mob.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY(), getBlockPos().getZ() + 0.5);
            mob.yHeadRot = entityYawHead;
            mob.yBodyRot = entityYawOffset;
            level.addFreshEntity(mob);
        }
    }

    /** Rebuilds the client-side display entity from the stored NBT, or returns the cached one. */
    @Nullable
    public Mob getOrCreateDisplayCreature() {
        if (displayCreature == null && entityName != null && level != null && !entityCompound.isEmpty()) {
            Entity entity = BuiltInRegistries.ENTITY_TYPE.get(entityName).create(level);
            if (entity instanceof Mob mob) {
                mob.load(entityCompound);
                mob.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY(), getBlockPos().getZ() + 0.5);
                mob.yHeadRot = entityYawHead;
                mob.yHeadRotO = entityYawHead;
                mob.yBodyRot = entityYawOffset;
                mob.yBodyRotO = entityYawOffset;
                mob.hurtTime = 0;
                displayCreature = mob;
            }
        }
        return displayCreature;
    }

    public void serverTick() {
        this.timer++;

        if (level == null || position != 1) return;

        if (!isIce) {
            // Petrified creatures break free in low light after the minimum duration, with a higher chance
            // the lower the light level: (8 - light)/12 every 10 seconds (1.12.2)
            if (this.timer % 200 == 0 && this.timer > lifetime) {
                int light = Math.max(level.getBrightness(LightLayer.BLOCK, getBlockPos()),
                        level.getBrightness(LightLayer.SKY, getBlockPos()));
                if (light < level.random.nextInt(12) - 3) {
                    level.destroyBlock(getBlockPos(), false);
                }
            }
        } else {
            // Ice statues simply melt when the duration ends
            if (this.timer > lifetime) {
                level.destroyBlock(getBlockPos(), false);
            }
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        position = tag.getInt("position");
        parts = tag.getInt("parts");
        entityCompound = tag.getCompound("entity");
        if (tag.contains("entityName")) entityName = ResourceLocation.tryParse(tag.getString("entityName"));
        timer = tag.getInt("timer");
        lifetime = tag.getInt("lifetime");
        isIce = tag.getBoolean("isIce");
        entityYawHead = tag.getFloat("entityYawHead");
        entityYawOffset = tag.getFloat("entityYawOffset");
        entityWidth = tag.getFloat("entityWidth");
        entityHeight = tag.getFloat("entityHeight");
        displayCreature = null; // Force the renderer to rebuild it
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("position", position);
        tag.putInt("parts", parts);
        tag.put("entity", entityCompound);
        if (entityName != null) tag.putString("entityName", entityName.toString());
        tag.putInt("timer", timer);
        tag.putInt("lifetime", lifetime);
        tag.putBoolean("isIce", isIce);
        tag.putFloat("entityYawHead", entityYawHead);
        tag.putFloat("entityYawOffset", entityYawOffset);
        tag.putFloat("entityWidth", entityWidth);
        tag.putFloat("entityHeight", entityHeight);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
