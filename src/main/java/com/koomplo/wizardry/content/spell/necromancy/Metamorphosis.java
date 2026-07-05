package com.koomplo.wizardry.content.spell.necromancy;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.core.EBLogger;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Metamorphosis extends RaySpell {

    /** Forward transformation mappings (A → B). Addons can add their own via {@link #addTransformation}. */
    public static final Map<EntityType<?>, EntityType<?>> TRANSFORMATIONS = new HashMap<>();
    /** Reverse transformation mappings (B → A), used when the caster is sneaking. */
    public static final Map<EntityType<?>, EntityType<?>> TRANSFORMATIONS_INVERSE = new HashMap<>();

    static {
        addTransformation(EntityType.PIG, EntityType.ZOMBIFIED_PIGLIN);
        addTransformation(EntityType.COW, EntityType.MOOSHROOM);
        addTransformation(EntityType.CHICKEN, EntityType.BAT);
        addTransformation(EntityType.ZOMBIE, EntityType.HUSK);
        addTransformation(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON);
        addTransformation(EntityType.SPIDER, EntityType.CAVE_SPIDER);
        addTransformation(EntityType.SLIME, EntityType.MAGMA_CUBE);
    }

    /**
     * Adds circular mappings between the given entity types to the transformations map. In other words, given an
     * array of entity types [A, B, C, D], adds mappings A → B, B → C, C → D and D → A.
     */
    public static void addTransformation(EntityType<?>... entities) {
        EntityType<?> previousEntity = entities[entities.length - 1];
        for (EntityType<?> entity : entities) {
            TRANSFORMATIONS.put(previousEntity, entity);
            TRANSFORMATIONS_INVERSE.put(entity, previousEntity);
            previousEntity = entity;
        }
    }

    @Override
    public boolean canCastByEntity() {
        return false;
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        Entity target = entityHit.getEntity();
        Level world = ctx.world();

        if (!EntityUtil.isLiving(target)) return false;

        double xPos = target.getX();
        double yPos = target.getY();
        double zPos = target.getZ();

        // Sneaking allows the entities to be cycled through in the other direction.
        // Dispensers always cycle through entities in the normal direction.
        EntityType<?> newEntityType = ctx.caster() != null && ctx.caster().isShiftKeyDown()
                ? TRANSFORMATIONS_INVERSE.get(target.getType()) : TRANSFORMATIONS.get(target.getType());

        if (newEntityType == null) return false;

        if (!world.isClientSide) {
            Entity newEntity = newEntityType.create(world);

            if (!(newEntity instanceof LivingEntity)) {
                EBLogger.error("Error while attempting to transform entity " + target.getType() + " to entity " + newEntityType);
                return false;
            }

            // Transfers attributes from the old entity to the new one
            CompoundTag tag = new CompoundTag();
            target.saveWithoutId(tag);
            // Remove the UUID because keeping it the same causes the entity to disappear
            tag.remove("UUID");
            newEntity.load(tag);

            target.discard();
            newEntity.setPos(xPos, yPos, zPos);
            world.addFreshEntity(newEntity);

        } else {
            for (int i = 0; i < 20; i++) {
                ParticleBuilder.create(EBParticles.DARK_MAGIC, world.random, xPos, yPos + 1, zPos, 1, false)
                        .color(0.1f, 0f, 0f).spawn(world);
            }
            ParticleBuilder.create(EBParticles.BUFF).pos(xPos, yPos, zPos).color(0xd363cb).spawn(world);
        }

        this.playSound(world, (LivingEntity) target, ctx.castingTicks(), -1);
        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.NECROMANCY, SpellType.UTILITY, SpellAction.POINT, 15, 0, 30)
                .add(DefaultProperties.RANGE, 10f)
                .build();
    }
}
