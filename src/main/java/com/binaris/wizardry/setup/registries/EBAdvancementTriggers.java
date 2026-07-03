package com.binaris.wizardry.setup.registries;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.content.advancement.SpellCastTrigger;
import com.binaris.wizardry.content.advancement.SpellDiscoveryTrigger;
import com.binaris.wizardry.content.advancement.WizardryAdvancementTrigger;
import com.binaris.wizardry.content.advancement.WizardryContainerTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;

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

    public static void register() {
        register("discover_spell", DISCOVER_SPELL);
        register("max_out_wand", MAX_OUT_WAND);
        register("special_upgrade", SPECIAL_UPGRADE);
        register("anger_wizard", ANGER_WIZARD);
        register("buy_master_spell", BUY_MASTER_SPELL);
        register("spell_failure", SPELL_FAILURE);
        register("wizard_trade", WIZARD_TRADE);
        register("wand_levelup", WAND_LEVELUP);
        register("restore_imbuement_altar", RESTORE_IMBUEMENT_ALTAR);
        register("cast_spell", CAST_SPELL);
        register("arcane_workbench", ARCANE_WORKBENCH);
        register("imbuement_altar", IMBUEMENT_ALTAR);
    }

    private static <T extends CriterionTrigger<?>> void register(String name, T trigger) {
        CriteriaTriggers.register(WizardryMainMod.location(name).toString(), trigger);
    }
}
