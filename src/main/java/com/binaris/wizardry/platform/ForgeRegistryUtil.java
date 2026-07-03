package com.binaris.wizardry.platform;

import com.binaris.wizardry.api.content.spell.Element;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellTier;
import com.binaris.wizardry.core.platform.services.IRegistryUtil;
import com.binaris.wizardry.registry.EBRegistriesForge;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ForgeRegistryUtil implements IRegistryUtil {
    // The DeferredRegister-backed registry suppliers return null until the registry is actually
    // created (NewRegistryEvent), which is well after class bootstrap. Some callers reach these
    // methods before that point — notably Minecraft's dev-only command ambiguity validation
    // (Bootstrap.validate -> Commands.validate -> ArgumentType.parse) — so every accessor must
    // tolerate a not-yet-created registry and degrade to empty/null instead of NPEing.

    @Override
    public Collection<Element> getElements() {
        Registry<Element> registry = EBRegistriesForge.ELEMENT.get();
        return registry == null ? List.of() : registry.stream().toList();
    }

    @Override
    public Collection<SpellTier> getTiers() {
        Registry<SpellTier> registry = EBRegistriesForge.TIER.get();
        return registry == null ? List.of() : registry.stream()
                .sorted(Comparator.comparingInt(SpellTier::getLevel))
                .toList();
    }

    @Override
    public Collection<Spell> getSpells() {
        Registry<Spell> registry = EBRegistriesForge.SPELL.get();
        return registry == null ? List.of() : registry.stream().toList();
    }

    @Override
    public @Nullable Element getElement(ResourceLocation location) {
        Registry<Element> registry = EBRegistriesForge.ELEMENT.get();
        return registry == null ? null : registry.get(location);
    }

    @Override
    public @Nullable SpellTier getTier(ResourceLocation location) {
        Registry<SpellTier> registry = EBRegistriesForge.TIER.get();
        return registry == null ? null : registry.get(location);
    }

    @Override
    public @Nullable Spell getSpell(ResourceLocation location) {
        Registry<Spell> registry = EBRegistriesForge.SPELL.get();
        return registry == null ? null : registry.get(location);
    }

    @Override
    public @Nullable ResourceLocation getSpell(Spell spell) {
        Registry<Spell> registry = EBRegistriesForge.SPELL.get();
        return registry == null ? null : registry.getKey(spell);
    }

    @Override
    public @Nullable ResourceLocation getElement(Element element) {
        Registry<Element> registry = EBRegistriesForge.ELEMENT.get();
        return registry == null ? null : registry.getKey(element);
    }

    @Override
    public @Nullable ResourceLocation getTier(SpellTier tier) {
        Registry<SpellTier> registry = EBRegistriesForge.TIER.get();
        return registry == null ? null : registry.getKey(tier);
    }
}
