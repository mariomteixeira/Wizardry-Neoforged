package com.koomplo.wizardry.api.content.util;

import com.koomplo.wizardry.api.content.item.IWorkbenchItem;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.registries.EBDataComponents;
import com.koomplo.wizardry.setup.registries.Spells;
import com.koomplo.wizardry.setup.registries.WandUpgrades;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class avoids big classes just to handle item data for getting/saving values.
 * <p>
 * Data is stored as data components (see {@link EBDataComponents}), replacing the former NBT layout:
 * <ul>
 *     <li>A list of spells under {@link EBDataComponents#SPELLS}, stored as a list of {@code ResourceLocation}s.</li>
 *     <li>The currently selected spell index under {@link EBDataComponents#SELECTED_SPELL}, stored as an integer.</li>
 *     <li>A list of longs under {@link EBDataComponents#COOLDOWN_END_TIMES}, storing the game time when each spell's cooldown ends.</li>
 *     <li>A list of integers under {@link EBDataComponents#MAX_COOLDOWNS}, storing the maximum cooldown for each spell slot.</li>
 *     <li>A map under {@link EBDataComponents#UPGRADES}, storing the levels of each upgrade.</li>
 *     <li>An integer under {@link EBDataComponents#PROGRESSION}, storing the item's progression level.</li>
 * </ul>
 */
public final class CastItemDataHelper {
    /** The key used to store the array of spells on the item in form of {@code ResourceLocation}. */
    public static final String SPELL_ARRAY_KEY = "spells";

    /** The key used to store the currently selected spell index on the item in form of an integer. */
    public static final String SELECTED_SPELL_KEY = "selectedSpell";

    /** The key used to store the array of cooldown end times (game time) for each spell on the item in form of an array of longs. */
    public static final String COOLDOWN_END_TIME_ARRAY_KEY = "cooldownEndTime";

    /** The key used to store the array of maximum cooldowns for each spell on the item in form of an array of longs. */
    public static final String MAX_COOLDOWN_ARRAY_KEY = "maxCooldown";

    /** The key used to store the item upgrades in form of an array of integers. */
    public static final String UPGRADES_KEY = "upgrades";

    /** The key used to store the item progression level in form of an integer. */
    public static final String PROGRESSION_KEY = "progression";

    /**
     * Gives the list of spells stored on the stack. If there are fewer spells stored than the item's maximum spell slots,
     * the list is padded with {@link Spells#NONE} to reach the maximum size.
     *
     * @param stack The ItemStack.
     * @return A list of spells on the item, padded with {@link Spells#NONE} up to the item's spell slot count even when
     * the stack has no spell data yet, so slot-indexed callers (e.g. workbench binding) always see every slot.
     */
    public static List<Spell> getSpells(ItemStack stack) {
        ArrayList<Spell> spells = new ArrayList<>();
        List<ResourceLocation> stored = stack.get(EBDataComponents.SPELLS.get());
        if (stored != null) {
            for (ResourceLocation location : stored) {
                spells.add(Services.REGISTRY_UTIL.getSpell(location));
            }
        }

        int maxSlots = stack.getItem() instanceof IWorkbenchItem workbenchItem ? workbenchItem.getSpellSlotCount(stack) : 5;
        while (spells.size() < maxSlots) spells.add(Spells.NONE);

        return spells;
    }

    /**
     * Sets the list of spells on the stack.
     *
     * @param stack  The ItemStack.
     * @param spells The collection of spells to set on the stack.
     */
    public static void setSpells(ItemStack stack, Collection<Spell> spells) {
        List<ResourceLocation> list = spells.stream().map(Spell::getLocation).toList();
        stack.set(EBDataComponents.SPELLS.get(), list);
    }

    /**
     * Gives the currently selected spell on the stack.
     *
     * @param stack The ItemStack.
     * @return The currently selected spell, or {@link Spells#NONE} if no spells are stored/selected.
     */
    public static Spell getCurrentSpell(ItemStack stack) {
        List<Spell> spells = getSpells(stack);
        if (spells.isEmpty()) return Spells.NONE;
        int selectedIndex = stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);

        // Bounds check
        if (selectedIndex >= 0 && selectedIndex < spells.size()) {
            return spells.get(selectedIndex);
        }

        return spells.get(0);
    }

    /**
     * Gives the index of the current spell selected by the stack.
     *
     * @param stack The ItemStack.
     * @return The index of the currently selected spell, or 0 if no spells are stored/selected.
     */
    public static int getCurrentSpellIndex(ItemStack stack) {
        return stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);
    }

    /**
     * Sets the currently selected spell on the stack based on its location in the list of saved spells.
     *
     * @param stack The ItemStack.
     * @param spell The spell to set as currently selected. If the spell is not in the list, the first spell is selected.
     */
    public static void setCurrentSpell(ItemStack stack, Spell spell) {
        List<Spell> spells = getSpells(stack);
        int index = spells.indexOf(spell);
        if (index == -1) index = 0;
        stack.set(EBDataComponents.SELECTED_SPELL.get(), index);
    }

    /**
     * Sets the currently selected spell on the stack by index based on the list of saved spells.
     *
     * @param stack The ItemStack.
     * @param index The index of the spell to set as currently selected.
     * @return True if the index was valid and the spell was set, false otherwise.
     */
    public static boolean setCurrentSpell(ItemStack stack, int index) {
        List<Spell> spells = getSpells(stack);
        if (index < 0 || index >= spells.size()) {
            return false;
        }
        stack.set(EBDataComponents.SELECTED_SPELL.get(), index);
        return true;
    }

    /**
     * Gives the next spell in the stack's spell list, wrapping around to the start if necessary.
     *
     * @param stack The ItemStack.
     * @return The next spell in the list based on the currently selected spell.
     */
    public static Spell getNextSpell(ItemStack stack) {
        return getAdjacentSpell(stack, 1);
    }

    /**
     * Returns the previous spell in the stack's spell list, wrapping around to the end if necessary.
     *
     * @param stack The ItemStack.
     * @return The previous spell in the list based on the currently selected spell.
     */
    public static Spell getPreviousSpell(ItemStack stack) {
        return getAdjacentSpell(stack, -1);
    }

    /**
     * Selects the next spell on the stack by incrementing the index, wrapping around if necessary.
     * This method directly manipulates indices to avoid issues with duplicate spells (e.g., multiple NONE spells).
     *
     * @param stack The ItemStack.
     */
    public static void selectNextSpell(ItemStack stack) {
        List<Spell> spells = getSpells(stack);
        if (spells.isEmpty()) return;
        int currentIndex = stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);

        // Bounds check
        if (currentIndex < 0 || currentIndex >= spells.size()) {
            currentIndex = 0;
        }

        int newIndex = (currentIndex + 1) % spells.size();
        stack.set(EBDataComponents.SELECTED_SPELL.get(), newIndex);
    }

    /**
     * Selects the previous spell on the stack by decrementing the index, wrapping around if necessary.
     * This method directly manipulates indices to avoid issues with duplicate spells (e.g., multiple NONE spells).
     *
     * @param stack The ItemStack.
     */
    public static void selectPreviousSpell(ItemStack stack) {
        List<Spell> spells = getSpells(stack);
        if (spells.isEmpty()) return;
        int currentIndex = stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);

        // Bounds check
        if (currentIndex < 0 || currentIndex >= spells.size()) {
            currentIndex = 0;
        }

        int newIndex = (currentIndex - 1 + spells.size()) % spells.size();
        stack.set(EBDataComponents.SELECTED_SPELL.get(), newIndex);
    }

    /**
     * Gives the wanted Spell on the saved Spells list based on the current selected spell and the offset.
     *
     * @param stack  The ItemStack.
     * @param offset The offset to apply to the current index.
     * @return The Spell at the new index. If the Spells list is empty, returns Spells.NONE.
     */
    private static Spell getAdjacentSpell(ItemStack stack, int offset) {
        List<Spell> spells = getSpells(stack);
        if (spells.isEmpty()) return Spells.NONE;
        int currentIndex = stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);

        // Bounds check
        if (currentIndex < 0 || currentIndex >= spells.size()) {
            currentIndex = 0;
        }

        int newIndex = (currentIndex + offset + spells.size()) % spells.size();
        return spells.get(newIndex);
    }

    /**
     * Gives the array of cooldown end times (level game time) for each spell on the stack.
     *
     * @param stack The ItemStack.
     * @return An array of cooldown end times. If none are stored, returns an empty array.
     */
    public static long[] getCooldownEndTimes(ItemStack stack) {
        List<Long> stored = stack.get(EBDataComponents.COOLDOWN_END_TIMES.get());
        if (stored == null) return new long[0];
        long[] endTimes = new long[stored.size()];
        for (int i = 0; i < endTimes.length; i++) endTimes[i] = stored.get(i);
        return endTimes;
    }

    /**
     * Sets the array of cooldown end times (level game time) for each spell on the stack.
     *
     * @param stack            The ItemStack.
     * @param cooldownEndTimes The array of cooldown end times to set.
     */
    public static void setCooldownEndTimes(ItemStack stack, long[] cooldownEndTimes) {
        List<Long> list = new ArrayList<>(cooldownEndTimes.length);
        for (long endTime : cooldownEndTimes) list.add(endTime);
        stack.set(EBDataComponents.COOLDOWN_END_TIMES.get(), list);
    }

    /**
     * Gives the current cooldown for the currently selected spell on the stack based on game time.
     *
     * @param stack           The ItemStack.
     * @param currentGameTime The current game time from the world.
     * @return The current cooldown remaining for the selected spell, or 0 if none.
     */
    public static int getCurrentCooldown(ItemStack stack, long currentGameTime) {
        long[] endTimes = getCooldownEndTimes(stack);
        int selectedSpellIndex = stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);

        if (selectedSpellIndex >= 0 && selectedSpellIndex < endTimes.length) {
            long endTime = endTimes[selectedSpellIndex];
            if (endTime > currentGameTime) {
                return (int) (endTime - currentGameTime);
            }
        }
        return 0;
    }

    /**
     * Sets the cooldown for the currently selected spell on the stack using game time.
     *
     * @param stack           The ItemStack.
     * @param cooldown        The cooldown duration in ticks.
     * @param currentGameTime The current game time from the world.
     */
    public static void setCurrentCooldown(ItemStack stack, int cooldown, long currentGameTime) {
        int selectedSpell = stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);
        int spellCount = getSpells(stack).size();

        if (selectedSpell >= spellCount) return;

        long[] endTimes = getCooldownEndTimes(stack);
        if (endTimes.length <= selectedSpell) endTimes = new long[spellCount];

        endTimes[selectedSpell] = currentGameTime + Math.max(1, cooldown);
        setCooldownEndTimes(stack, endTimes);

        int[] maxCooldowns = getMaxCooldowns(stack);
        if (maxCooldowns.length <= selectedSpell) maxCooldowns = new int[spellCount];
        maxCooldowns[selectedSpell] = Math.max(1, cooldown);
        setMaxCooldowns(stack, maxCooldowns);
    }

    /**
     * Gives the array of maximum cooldowns for each spell on the stack.
     *
     * @param stack The ItemStack.
     * @return An array of maximum cooldowns, or an empty array if none are stored.
     */
    public static int[] getMaxCooldowns(ItemStack stack) {
        List<Integer> stored = stack.get(EBDataComponents.MAX_COOLDOWNS.get());
        if (stored == null) return new int[0];
        int[] cooldowns = new int[stored.size()];
        for (int i = 0; i < cooldowns.length; i++) cooldowns[i] = stored.get(i);
        return cooldowns;
    }

    /**
     * Sets the array of maximum cooldowns for each spell on the stack.
     *
     * @param stack     The ItemStack.
     * @param cooldowns The array of maximum cooldowns to set.
     */
    public static void setMaxCooldowns(ItemStack stack, int[] cooldowns) {
        List<Integer> list = new ArrayList<>(cooldowns.length);
        for (int cooldown : cooldowns) list.add(cooldown);
        stack.set(EBDataComponents.MAX_COOLDOWNS.get(), list);
    }

    /**
     * Gives the maximum cooldown for the currently selected spell on the stack.
     *
     * @param stack The ItemStack.
     * @return The maximum cooldown for the selected spell, or 0 if not set.
     */
    public static int getCurrentMaxCooldown(ItemStack stack) {
        int[] cooldowns = getMaxCooldowns(stack);
        int selectedSpell = stack.getOrDefault(EBDataComponents.SELECTED_SPELL.get(), 0);

        return (selectedSpell >= 0 && selectedSpell < cooldowns.length) ? cooldowns[selectedSpell] : 0;
    }

    /**
     * Gives the level of the specified upgrade on the stack.
     *
     * @param stack   The ItemStack.
     * @param upgrade The upgrade item.
     * @return The level of the upgrade, or 0 if the upgrade is not found.
     */
    public static int getUpgradeLevel(ItemStack stack, Item upgrade) {
        Map<String, Integer> upgrades = stack.get(EBDataComponents.UPGRADES.get());
        if (upgrades == null) return 0;

        for (var entry : WandUpgrades.getWandUpgrades().entrySet()) {
            if (entry.getKey().equals(upgrade)) {
                return upgrades.getOrDefault(entry.getValue(), 0);
            }
        }
        return 0;
    }

    /**
     * Gives the total number of upgrades applied to the stack.
     *
     * @param stack The ItemStack.
     * @return The total number of upgrades, could be 0 if no upgrades are applied.
     */
    public static int getTotalUpgrades(ItemStack stack) {
        return WandUpgrades.getWandUpgrades().keySet().stream()
                .mapToInt(item -> getUpgradeLevel(stack, item))
                .sum();
    }

    /**
     * Applies the specified upgrade to the stack, increasing its level by 1 each time.
     *
     * @param stack   The ItemStack.
     * @param upgrade The upgrade item.
     */
    public static void applyUpgrade(ItemStack stack, Item upgrade) {
        Map<String, Integer> current = stack.get(EBDataComponents.UPGRADES.get());
        Map<String, Integer> upgrades = current == null ? new HashMap<>() : new HashMap<>(current);

        for (var entry : WandUpgrades.getWandUpgrades().entrySet()) {
            if (entry.getKey().equals(upgrade)) {
                String key = entry.getValue();
                upgrades.put(key, upgrades.getOrDefault(key, 0) + 1);
                stack.set(EBDataComponents.UPGRADES.get(), upgrades);
                return;
            }
        }
    }

    /**
     * Returns the stack's current progression level.
     *
     * @param stack The ItemStack.
     * @return The stack's progression level. If unset, returns 0.
     */
    public static int getProgression(ItemStack stack) {
        return stack.getOrDefault(EBDataComponents.PROGRESSION.get(), 0);
    }

    /**
     * Sets the stack's progression level to the specified value.
     *
     * @param stack       The ItemStack.
     * @param progression The progression level to set.
     */
    public static void setProgression(ItemStack stack, int progression) {
        stack.set(EBDataComponents.PROGRESSION.get(), progression);
    }

    /**
     * Increases the stack's progression level by the specified amount.
     *
     * @param stack       The ItemStack.
     * @param progression The amount to increase the progression level by.
     */
    public static void addProgression(ItemStack stack, int progression) {
        setProgression(stack, getProgression(stack) + progression);
    }

    private CastItemDataHelper() {
    }
}
