package com.koomplo.wizardry.content.spell.healing;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.entity.living.ISpellCaster;
import com.koomplo.wizardry.api.content.event.SpellCastEvent;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.AreaEffectSpell;
import com.koomplo.wizardry.content.spell.abstr.BuffSpell;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.Spells;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class EmpoweringPresence extends AreaEffectSpell {

    /** The fraction by which potency is increased per level of the empowerment effect. */
    public static final SpellProperty<Float> POTENCY_PER_LEVEL = SpellProperty.floatProperty("potency_per_level");

    public EmpoweringPresence() {
        super();
        this.targetAllies(true);
        this.alwaysSucceed(true);
    }

    // Empowerment stacks extra potency on top of the existing potency (1.12.2 parity).
    public static void onSpellCastPreEvent(SpellCastEvent.Pre event) {
        if (event.getCaster() != null
                && event.getCaster().hasEffect(EBMobEffects.holder(EBMobEffects.EMPOWERMENT))
                && !(event.getSpell() instanceof EmpoweringPresence)) { // Prevent exponential empowerment stacking!

            MobEffectInstance inst = event.getCaster().getEffect(EBMobEffects.holder(EBMobEffects.EMPOWERMENT));
            if (inst == null) return;

            float potency = 1 + Spells.EMPOWERING_PRESENCE.property(POTENCY_PER_LEVEL) * (inst.getAmplifier() + 1);
            event.getModifiers().set(SpellModifiers.POTENCY, event.getModifiers().get(SpellModifiers.POTENCY) * potency);
        }
    }

    @Override
    protected boolean affectEntity(CastContext ctx, Vec3 origin, LivingEntity target, int targetCount) {
        if (target instanceof Player || target instanceof ISpellCaster) { // Only useful for spell casters
            int bonusAmplifier = BuffSpell.getStandardBonusAmplifier(ctx.modifiers().get(SpellModifiers.POTENCY));

            if (!ctx.world().isClientSide) {
                target.addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.EMPOWERMENT),
                        (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)),
                        property(DefaultProperties.EFFECT_STRENGTH) + bonusAmplifier));
            }
        }
        return true;
    }

    @Override
    protected void spawnParticle(Level world, double x, double y, double z) {
        ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).velocity(0, 0.03, 0).time(50).color(0.5f, 0.4f, 0.75f).spawn(world);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.HEALING, SpellType.BUFF, SpellAction.POINT_UP, 30, 20, 60)
                .add(DefaultProperties.EFFECT_RADIUS, 5)
                .add(DefaultProperties.EFFECT_DURATION, 900)
                .add(DefaultProperties.EFFECT_STRENGTH, 0)
                .add(POTENCY_PER_LEVEL, 0.15f)
                .build();
    }
}
