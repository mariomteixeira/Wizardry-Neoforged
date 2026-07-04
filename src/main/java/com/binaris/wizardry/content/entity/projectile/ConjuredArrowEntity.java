package com.binaris.wizardry.content.entity.projectile;

import com.binaris.wizardry.setup.registries.EBEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ConjuredArrowEntity extends AbstractArrow {
    private int knockback = 0;

    public ConjuredArrowEntity(EntityType<ConjuredArrowEntity> entityType, Level world) {
        super(entityType, world);
    }

    public ConjuredArrowEntity(double x, double y, double z, Level level) {
        super(EBEntities.CONJURED_ARROW.get(), x, y, z, level, ItemStack.EMPTY, null);
    }

    public ConjuredArrowEntity(EntityType<? extends AbstractArrow> type, LivingEntity entity, Level level) {
        super(type, entity, level, ItemStack.EMPTY, null);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 60) this.discard();
        super.tick();
    }

    public int getKnockback() {
        return this.knockback;
    }

    public void setKnockback(int knockback) {
        this.knockback = knockback;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.knockback <= 0 || !(this.level() instanceof ServerLevel)) return;
        if (!(hitResult.getEntity() instanceof LivingEntity target)) return;

        double factor = Math.max(0.0, 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 push = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double) this.knockback * 0.6 * factor);
        if (push.lengthSqr() > 0.0) target.push(push.x, 0.1, push.z);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        // Never EMPTY: AbstractArrow saves this stack unconditionally and ItemStack.save throws on empty
        // stacks, killing entity persistence. Pickup remains blocked via getPickupItem/pickup mode.
        return new ItemStack(net.minecraft.world.item.Items.ARROW);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
