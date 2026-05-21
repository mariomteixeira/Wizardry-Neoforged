package com.binaris.wizardry.core.gametest;

import com.binaris.wizardry.api.content.spell.Spell;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;

public final class EBTestCentral {

    public static class ArcaneWorkbench {
        /**
         * Applies the given spells to the wand using a dummy arcane workbench. It could fail if the spells aren't inside the
         * wand after being applied.
         */
        public static void applySpellsToWand(GameTestHelper helper, Item wand, Spell... spells) {
            ArcaneWorkbenchTestHandler.applySpellsToWand(helper, wand, spells);
        }

        /**
         * Different to {@link #canUpgradeToNextTier}. <p>
         * Checks if the given wand can be upgraded to the next tier (in case if it's not a master wand) based on a NBT arcane tome
         */
        public static void upgradeWandNextTierNBT(GameTestHelper helper, Item wand) {
            ArcaneWorkbenchTestHandler.upgradeWandNextTierNBT(helper, wand);
        }

        /**
         * Different to {@link #upgradeWandNextTierNBT}. <p>
         * Checks if the given wand can be upgraded to the next tier (in case if it's not a master wand) based on an item
         * instance based arcane tome.
         */
        public static void canUpgradeToNextTier(GameTestHelper helper, Item wand) {
            ArcaneWorkbenchTestHandler.canUpgradeToNextTier(helper, wand);
        }

        /** Puts the given spell into a new spell scroll using a blank scroll */
        public static void putSpellOnBlankScroll(GameTestHelper helper, Spell spell) {
            ArcaneWorkbenchTestHandler.putSpellOnBlankScroll(helper, spell);
        }

        /** Try to put a spell into a not blank scroll, should be false */
        public static void putSpellOnScrollFilled(GameTestHelper helper) {
            ArcaneWorkbenchTestHandler.putSpellOnScrollFilled(helper);
        }

        /** Tests upgrading normal wizard armor to a higher tier using an upgrade item. */
        public static void upgradeNormalArmor(GameTestHelper helper, Item armor, Item upgradeItem) {
            ArcaneWorkbenchTestHandler.upgradeNormalArmor(helper, armor, upgradeItem);
        }

        /** Tests that maxed out wizard armor cannot be upgraded further. */
        public static void cannotUpgradeMaxedArmor(GameTestHelper helper, Item armor, Item upgradeItem) {
            ArcaneWorkbenchTestHandler.cannotUpgradeMaxedArmor(helper, armor, upgradeItem);
        }

        /** Tests repairing a damaged wand using 1 crystal. */
        public static void repairWand(GameTestHelper helper, Item wand, Item crystal) {
            ArcaneWorkbenchTestHandler.repairWand(helper, wand, crystal);
        }

        /** Tests that blank scrolls cannot exceed the stack limit of 16 in the workbench center slot. */
        public static void cannotExceedBlankScrollLimit(GameTestHelper helper) {
            ArcaneWorkbenchTestHandler.cannotExceedBlankScrollLimit(helper);
        }

        /** Tests that spell books cannot exceed the stack limit of 1 per slot in the workbench. */
        public static void cannotExceedSpellBookLimit(GameTestHelper helper, Spell spell) {
            ArcaneWorkbenchTestHandler.cannotExceedSpellBookLimit(helper, spell);
        }

        /** Tests that upgrade items cannot exceed the stack limit of 1 in the upgrade slot. */
        public static void cannotExceedUpgradeLimit(GameTestHelper helper, Item upgradeItem) {
            ArcaneWorkbenchTestHandler.cannotExceedUpgradeLimit(helper, upgradeItem);
        }
    }

    public static class ConjureSpells {
        /**
         * Test if the Conjure Item Spells are working, spawning the item and placing it in the player's inventory, taking the
         * spell Flamecatcher as an example.
         */
        public static void spawnConjureItem(GameTestHelper helper) {
            ConjureSpellsTestHandler.spawnConjureItem(helper);
        }

        /** Test if the Conjure Item Spells are working, despawing the item after the time limit */
        public static void conjureItemDespawn(GameTestHelper helper) {
            ConjureSpellsTestHandler.conjureItemDespawn(helper);
        }

        public static void buildTable(GameTestHelper helper) {
            ConjureSpellsTestHandler.buildTable(helper);
        }
    }

    public static class TempEnchants {
        /**
         * Puts an imbuement enchant to an item and check if the enchant is present
         */
        public static void simpleEnchantTest(GameTestHelper helper) {
            TempEnchantsTestHandler.simpleEnchantTest(helper);
        }

        /**
         * Puts an imbuement enchant alongside a normal enchant, checks if both are present
         */
        public static void simpleEnchantCompanyTest(GameTestHelper helper) {
            TempEnchantsTestHandler.simpleEnchantCompanyTest(helper);
        }

        /**
         * Puts an imbuement enchant to an item and checks after the end time if the enchantment is removed
         */
        public static  void imbuementTickTest(GameTestHelper helper) {
            TempEnchantsTestHandler.imbuementTickTest(helper);
        }

        /**
         * Puts an imbuement enchant alongside a normal enchant, checks if the normal enchant is still present and the imbuement is removed after the end time
         */
        public static void imbuementTickCompanyTest(GameTestHelper helper) {
            TempEnchantsTestHandler.imbuementTickCompanyTest(helper);
        }
    }

    public static class WizardArmor {

        public static void armorNeverBreaks(GameTestHelper helper) {
            WizardArmorTestHandler.armorNeverBreaks(helper);
        }

        public static void armorAttributesWithMana(GameTestHelper helper) {
            WizardArmorTestHandler.armorAttributesWithMana(helper);
        }

        public static void armorNoAttributesWithoutMana(GameTestHelper helper) {
            WizardArmorTestHandler.armorNoAttributesWithoutMana(helper);
        }
    }

    public static class Wand {
        /**
         * Tests basic spell navigation through a wand's spell list. Basically, checks if the previous/next spell selection
         * on wand is working.
         */
        public static void wandBasicMovement(GameTestHelper helper) {
            WandTestHandler.wandBasicMovement(helper);
        }

        /**
         * Tests spell navigation when a wand has only one spell loaded, this should be cycling starting on that spell and
         * then return to it
         */
        public static void wandPartiallyEmpty(GameTestHelper helper) {
            WandTestHandler.wandPartiallyEmpty(helper);
        }

        /**
         * Verifies that selecting next/previous spells wraps around correctly when cyclin through all spell slots multiple
         * times.
         */
        public static void wandCircularSelection(GameTestHelper helper) {
            WandTestHandler.wandCircularSelection(helper);
        }

        /**
         * Verifies that selecting a specific spell slot by literal index correctly
         * changes the active spell to the spell at that index.
         */
        public static void wandLiteralIndex(GameTestHelper helper) {
            WandTestHandler.wandLiteralIndex(helper);
        }

        /** Tests direct spell selection by index on a wand with only one spell loaded. */
        public static void wandLiteralIndexPartiallyEmpty(GameTestHelper helper) {
            WandTestHandler.wandLiteralIndexPartiallyEmpty(helper);
        }

        /** Tests the siphon upgrade's behavior when a player kills a mob. */
        public static void siphonUpgradePlayerKillMob(GameTestHelper helper) {
            WandTestHandler.siphonUpgradePlayerKillMob(helper);
        }
    }

    private EBTestCentral() {
    }
}
