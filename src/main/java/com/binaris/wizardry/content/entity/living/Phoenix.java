package com.binaris.wizardry.content.entity.living;

import com.binaris.wizardry.api.content.entity.living.ISpellCaster;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.util.BlockUtil;
import com.binaris.wizardry.content.entity.goal.AttackSpellGoal;
import com.binaris.wizardry.setup.registries.EBEntities;
import com.binaris.wizardry.setup.registries.EBSounds;
import com.binaris.wizardry.setup.registries.Spells;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Phoenix extends Monster implements ISpellCaster {

    private static final double AI_SPEED = 0.5;

    private Spell continuousSpell = Spells.NONE;
    private int spellCounter;

    public Phoenix(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public Phoenix(Level level) {
        this(EBEntities.PHOENIX.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, AI_SPEED);
    }

    @Override
    protected void registerGoals() {
        // Can attack for 7 seconds, then must cool down for 3 (1.12.2 EntityAIAttackSpell params)
        this.goalSelector.addGoal(1, new AttackSpellGoal<>(this, AI_SPEED, 15f, 60, 140));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public @NotNull List<Spell> getSpells() {
        return List.of(Spells.FLAME_RAY);
    }

    @Override
    public @NotNull Spell getContinuousSpell() {
        return continuousSpell;
    }

    @Override
    public void setContinuousSpell(Spell spell) {
        this.continuousSpell = spell;
    }

    @Override
    public int getSpellCounter() {
        return spellCounter;
    }

    @Override
    public void setSpellCounter(int count) {
        this.spellCounter = count;
    }

    @Override
    public void aiStep() {
        // Makes the phoenix hover 2-3 blocks above the floor
        Integer floorLevel = BlockUtil.getNearestFloor(level(), blockPosition(), 4);
        Vec3 motion = getDeltaMovement();

        if (floorLevel == null || this.getY() - floorLevel > 3) {
            this.setDeltaMovement(motion.x, -0.1, motion.z);
        } else if (this.getY() - floorLevel < 2) {
            this.setDeltaMovement(motion.x, 0.1, motion.z);
        } else {
            this.setDeltaMovement(motion.x, 0.0, motion.z);
        }

        if (this.random.nextInt(24) == 0) {
            this.playSound(EBSounds.ENTITY_PHOENIX_BURN.get(), 1.0F + this.random.nextFloat(),
                    this.random.nextFloat() * 0.7F + 0.3F);
        }

        // Flapping sound effect
        if (this.tickCount % 22 == 0) {
            this.playSound(EBSounds.ENTITY_PHOENIX_FLAP.get(), 1.0F, 1.0F);
        }

        if (level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                        this.getY() + this.getBbHeight() / 2 + this.random.nextDouble() * this.getBbHeight() / 2,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(), 0.0D, -0.1D, 0.0D);
            }
        }

        // Allows the phoenix to attack despite being in the air (1.12.2 quirk, kept for parity)
        this.setOnGround(true);

        super.aiStep();
    }

    @Override
    public void spawnAnim() {
        super.spawnAnim();
        spawnParticles();
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.deathTime == 1) this.spawnParticles();
    }

    private void spawnParticles() {
        if (!level().isClientSide) return;

        for (int i = 0; i < 15; i++) {
            level().addParticle(ParticleTypes.FLAME, this.getX() + this.random.nextFloat() - 0.5f,
                    this.getY() + this.random.nextFloat() * this.getBbHeight(),
                    this.getZ() + this.random.nextFloat() - 0.5f, 0, 0, 0);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false; // Immune to fall damage
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return EBSounds.ENTITY_PHOENIX_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return EBSounds.ENTITY_PHOENIX_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return EBSounds.ENTITY_PHOENIX_DEATH.get();
    }
}
