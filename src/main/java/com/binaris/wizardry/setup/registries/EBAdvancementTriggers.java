package com.binaris.wizardry.setup.registries;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.content.advancement.SpellCastTrigger;
import com.binaris.wizardry.content.advancement.SpellDiscoveryTrigger;
import com.binaris.wizardry.content.advancement.WizardryAdvancementTrigger;
import com.binaris.wizardry.content.advancement.WizardryContainerTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;

public final class EBAdvancementTriggers {
    public static final WizardryAdvancementTrigger MAX_OUT_WAND = new WizardryAdvancementTrigger("max_out_wand");
    public static final WizardryAdvancementTrigger SPECIAL_UPGRADE = new WizardryAdvancementTrigger("special_upgrade");
    public static final WizardryAdvancementTrigger ANGER_WIZARD = new WizardryAdvancementTrigger("anger_wizard");
    public static final WizardryAdvancementTrigger BUY_MASTER_SPELL = new WizardryAdvancementTrigger("buy_master_spell");
    public static final WizardryAdvancementTrigger WIZARD_TRADE = new WizardryAdvancementTrigger("wizard_trade");
    public static final WizardryAdvancementTrigger SPELL_FAILURE = new WizardryAdvancementTrigger("spell_failure");
    public static final WizardryAdvancementTrigger WAND_LEVELUP = new WizardryAdvancementTrigger("wand_levelup");
    public static final WizardryAdvancementTrigger RESTORE_IMBUEMENT_ALTAR = new WizardryAdvancementTrigger("restore_imbuement_altar");
    public static final SpellCastTrigger CAST_SPELL = new SpellCastTrigger();
    public static final SpellDiscoveryTrigger DISCOVER_SPELL = new SpellDiscoveryTrigger();
    public static final WizardryContainerTrigger ARCANE_WORKBENCH = new WizardryContainerTrigger("arcane_workbench");
    public static final WizardryContainerTrigger IMBUEMENT_ALTAR = new WizardryContainerTrigger("imbuement_altar");

    private EBAdvancementTriggers() {
    }

    /**
     * Registered via the {@code Registries.TRIGGER_TYPE} branch of
     * {@code WizardryForgeEvents.ModBusEvents#registerContent} rather than eagerly at mod construction time:
     * {@code BuiltInRegistries.TRIGGER_TYPES} is frozen by the time the mod constructor runs in 1.21, so calling
     * {@code CriteriaTriggers.register} there throws "Registry is already frozen".
     */
    public static void register(RegisterFunction<CriterionTrigger<?>> function) {
        register(function, "discover_spell", DISCOVER_SPELL);
        register(function, "max_out_wand", MAX_OUT_WAND);
        register(function, "special_upgrade", SPECIAL_UPGRADE);
        register(function, "anger_wizard", ANGER_WIZARD);
        register(function, "buy_master_spell", BUY_MASTER_SPELL);
        register(function, "spell_failure", SPELL_FAILURE);
        register(function, "wizard_trade", WIZARD_TRADE);
        register(function, "wand_levelup", WAND_LEVELUP);
        register(function, "restore_imbuement_altar", RESTORE_IMBUEMENT_ALTAR);
        register(function, "cast_spell", CAST_SPELL);
        register(function, "arcane_workbench", ARCANE_WORKBENCH);
        register(function, "imbuement_altar", IMBUEMENT_ALTAR);
    }

    private static void register(RegisterFunction<CriterionTrigger<?>> function, String name, CriterionTrigger<?> trigger) {
        function.register(BuiltInRegistries.TRIGGER_TYPES, WizardryMainMod.location(name), trigger);
    }
}
