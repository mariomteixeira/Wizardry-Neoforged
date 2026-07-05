package com.koomplo.wizardry.content.spell.ice;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.data.ImbuementEnchantData;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.InventoryUtil;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.sorcery.ImbueWeapon;
import com.koomplo.wizardry.core.config.EBServerConfig;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public class FreezingWeapon extends Spell {

    public static final ResourceKey<Enchantment> FREEZING_WEAPON = ResourceKey.create(Registries.ENCHANTMENT, WizardryMainMod.location("freezing_weapon"));

    @Override
    public boolean cast(PlayerCastContext ctx) {
        HolderLookup.RegistryLookup<Enchantment> enchants = ctx.world().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> freezing = enchants.getOrThrow(FREEZING_WEAPON);

        for (ItemStack stack : InventoryUtil.getHotBarAndOffhand(ctx.caster())) {
            if ((!ImbueWeapon.isSword(stack) && !ImbueWeapon.isBow(stack))
                    || stack.getEnchantments().getLevel(freezing) > 0)
                continue;

            ImbuementEnchantData data = Services.OBJECT_DATA.getImbuementData(stack);
            if (data == null) continue;

            int level = ctx.modifiers().get(SpellModifiers.POTENCY) == 1.0f ? 1
                    : (int) ((ctx.modifiers().get(SpellModifiers.POTENCY) - 1.0f) / EBServerConfig.POTENCY_INCREASE_PER_TIER.get() + 0.5f);
            long duration = (long) (ctx.world().getGameTime() + (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)));

            stack.enchant(freezing, level);
            data.addImbuement(freezing, duration);

            if (ctx.world().isClientSide) {
                for (int i = 0; i < 10; i++) {
                    double x = ctx.caster().getX() + ctx.world().random.nextDouble() * 2 - 1;
                    double y = ctx.caster().getY() + ctx.caster().getEyeHeight() - 0.5 + ctx.world().random.nextDouble();
                    double z = ctx.caster().getZ() + ctx.world().random.nextDouble() * 2 - 1;
                    ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z)
                            .velocity(0, 0.1, 0).color(0.6f, 0.8f, 1f).spawn(ctx.world());
                }
            }

            this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
            return true;
        }
        return false;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.ICE, SpellType.UTILITY, SpellAction.IMBUE, 35, 0, 70)
                .add(DefaultProperties.EFFECT_DURATION, 900)
                .build();
    }
}
