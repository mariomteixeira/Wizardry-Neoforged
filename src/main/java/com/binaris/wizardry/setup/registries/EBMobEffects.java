package com.binaris.wizardry.setup.registries;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.DeferredObject;
import com.binaris.wizardry.api.content.effect.CurseMobEffect;
import com.binaris.wizardry.api.content.effect.MagicMobEffect;
import com.binaris.wizardry.content.effect.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class EBMobEffects {
    static Map<String, DeferredObject<MobEffect>> MOB_EFFECTS = new HashMap<>();

    public static final DeferredObject<MobEffect> ARCANE_JAMMER = mobEffect("arcane_jammer", () -> new CurseMobEffect(MobEffectCategory.HARMFUL, 0)); // TODO
    public static final DeferredObject<MobEffect> FROST = mobEffect("frost", FrostMobEffect::new);
    public static final DeferredObject<MobEffect> PARALYSIS = mobEffect("paralysis", ParalysisMobEffect::new);
    public static final DeferredObject<MobEffect> STATIC_AURA = mobEffect("static_aura", StaticAuraMobEffect::new);
    public static final DeferredObject<MobEffect> WARD = mobEffect("ward", WardMobEffect::new);
    public static final DeferredObject<MobEffect> FIRESKIN = mobEffect("fireskin", FireSkinMobEffect::new);
    public static final DeferredObject<MobEffect> OAKFLESH = mobEffect("oakflesh", OakFleshMobEffect::new);
    public static final DeferredObject<MobEffect> IRONFLESH = mobEffect("ironflesh", IronfleshMobEffect::new);
    public static final DeferredObject<MobEffect> DIAMONDFLESH = mobEffect("diamondflesh", DiamondfleshMobEffect::new);
    public static final DeferredObject<MobEffect> MUFFLE = mobEffect("muffle", () -> new MagicMobEffect(MobEffectCategory.BENEFICIAL, 0x4464d9) {
        @Override
        public void spawnCustomParticle(net.minecraft.world.level.Level world, double x, double y, double z) {
        }
    });
    public static final DeferredObject<MobEffect> CURSE_OF_ENFEEBLEMENT = mobEffect("curse_of_enfeeblement", EnfeeblementCurse::new);
    public static final DeferredObject<MobEffect> CURSE_OF_UNDEATH = mobEffect("curse_of_undeath", UndeathCurse::new);
    public static final DeferredObject<MobEffect> DECAY = mobEffect("decay", DecayMobEffect::new);
    public static final DeferredObject<MobEffect> CONTAINMENT = mobEffect("containment", ContainmentEffect::new);
    public static final DeferredObject<MobEffect> FONT_OF_MANA = mobEffect("font_of_mana", FontOfManaMobEffect::new);
    public static final DeferredObject<MobEffect> CURSE_OF_SOULBINDING = mobEffect("curse_of_soulbinding", () -> new CurseMobEffect(MobEffectCategory.HARMFUL, 0x0f000f));
    public static final DeferredObject<MobEffect> FROST_STEP = mobEffect("frost_step", FrostStepEffect::new);

    // ======= Registry =======
    public static void register(RegisterFunction<MobEffect> function) {
        MOB_EFFECTS.forEach(((id, mobEffect) ->
                function.register(BuiltInRegistries.MOB_EFFECT, WizardryMainMod.location(id), mobEffect.get())));
    }

    /**
     * Wraps one of the mod's registered effects as a {@link Holder}, as required by the 1.21 effect APIs
     * ({@code LivingEntity.hasEffect/getEffect/removeEffect}, {@code MobEffectInstance} ctor).
     */
    public static Holder<MobEffect> holder(DeferredObject<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get());
    }

    // ======= Helpers =======
    private static DeferredObject<MobEffect> mobEffect(String name, Supplier<MobEffect> effect) {
        DeferredObject<MobEffect> ret = new DeferredObject<>(effect);
        MOB_EFFECTS.put(name, ret);
        return ret;
    }

}
