package com.koomplo.wizardry;

import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.api.content.event.*;
import com.koomplo.wizardry.api.content.item.ArtifactItem;
import com.koomplo.wizardry.api.content.spell.SpellContext;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.content.Forfeit;
import com.koomplo.wizardry.content.WizardryAttributeModifier;
import com.koomplo.wizardry.content.data.SpellGlyphData;
import com.koomplo.wizardry.content.effect.ContainmentEffect;
import com.koomplo.wizardry.content.effect.FireSkinMobEffect;
import com.koomplo.wizardry.content.effect.IceShroudMobEffect;
import com.koomplo.wizardry.content.effect.StaticAuraMobEffect;
import com.koomplo.wizardry.content.effect.WardMobEffect;
import com.koomplo.wizardry.content.entity.construct.BubbleConstruct;
import com.koomplo.wizardry.content.item.RandomSpellBookItem;
import com.koomplo.wizardry.content.item.WandUpgradeItem;
import com.koomplo.wizardry.content.item.armor.WizardArmorItem;
import com.koomplo.wizardry.content.spell.healing.ArcaneJammer;
import com.koomplo.wizardry.content.spell.healing.FontOfMana;
import com.koomplo.wizardry.content.spell.lightning.Charge;
import com.koomplo.wizardry.content.spell.necromancy.CurseOfSoulbinding;
import com.koomplo.wizardry.content.spell.sorcery.ArcaneLockSpell;
import com.koomplo.wizardry.core.AllyDesignation;
import com.koomplo.wizardry.core.ArtifactUtils;
import com.koomplo.wizardry.core.DataEvents;
import com.koomplo.wizardry.core.config.ConfigManager;
import com.koomplo.wizardry.core.event.WizardryEventBus;
import com.koomplo.wizardry.core.integrations.ArtifactChannel;
import com.koomplo.wizardry.setup.registries.EBAdvancementTriggers;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.LevelResource;

import java.util.Optional;

/**
 * Simple class to save all the event helper methods
 * This is internal use, you're not supposed to use this for any reason
 */
public final class EBEventHelper {
    private EBEventHelper() {
    }

    public static void register() {
        WizardryEventBus bus = WizardryEventBus.getInstance();
        onLivingHurtEvent(bus);
        onLivingTickEvent(bus);
        onSpellPreCast(bus);
        onSpellPostCast(bus);
        onServerLevelLoad(bus);
        onSpellTickCast(bus);
        onPlayerJoin(bus);
        onLivingDeathEvent(bus);
        onItemTossEvent(bus);
        onEntityJoinLevel(bus);
        onSpellDiscovery(bus);
        onPlayerInteractEntity(bus);
        onItemPlaceInContainer(bus);
        onPlayerUseBlock(bus);
        onPlayerBreakBlock(bus);
        onServerLoad(bus);
        onItemPickup(bus);
        onPlayerItemPickup(bus);
    }

    private static void onLivingHurtEvent(WizardryEventBus bus) {
        bus.register(EBLivingHurtEvent.class, Charge::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, StaticAuraMobEffect::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, IceShroudMobEffect::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, com.koomplo.wizardry.content.spell.healing.Transience::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, com.koomplo.wizardry.content.spell.healing.MarkSacrifice::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, com.koomplo.wizardry.content.spell.necromancy.MindTrick::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, com.koomplo.wizardry.content.spell.lightning.Paralysis::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, com.koomplo.wizardry.content.spell.lightning.LightningBoltSpell::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, FireSkinMobEffect::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, CurseOfSoulbinding::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, WardMobEffect::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, BubbleConstruct::onLivingHurt);
        bus.register(EBLivingHurtEvent.class, ArtifactItem::onHurtEntity);
        bus.register(EBLivingHurtEvent.class, ArtifactItem::onPlayerHurt);
        bus.register(EBLivingHurtEvent.class, AllyDesignation::onLivingHurt);
    }

    private static void onLivingTickEvent(WizardryEventBus bus) {
        bus.register(EBLivingTick.class, MagicMobEffect::onLivingTick);
        bus.register(EBLivingTick.class, com.koomplo.wizardry.content.effect.SlowTimeMobEffect::onLivingTick);
        bus.register(EBLivingTick.class, com.koomplo.wizardry.content.spell.necromancy.Intimidate::onLivingTick);
        bus.register(EBLivingTick.class, com.koomplo.wizardry.content.spell.lightning.Paralysis::onLivingTick);
        bus.register(EBLivingTick.class, ArtifactItem::onTick);
        bus.register(EBLivingTick.class, DataEvents::onMinionTick);
        bus.register(EBLivingTick.class, DataEvents::onPlayerTick);
        bus.register(EBLivingTick.class, ContainmentEffect::onLivingUpdateEvent);
        bus.register(EBLivingTick.class, com.koomplo.wizardry.content.spell.sorcery.Transportation::onLivingTick);
        bus.register(EBLivingTick.class, com.koomplo.wizardry.content.spell.earth.Glide::onLivingTick);
        bus.register(EBLivingTick.class, com.koomplo.wizardry.content.spell.necromancy.MindControl::onLivingTick);
        bus.register(EBLivingTick.class, com.koomplo.wizardry.content.spell.necromancy.Possession::onLivingTick);
    }

    private static void onSpellDiscovery(WizardryEventBus bus) {
        bus.register(EBDiscoverSpellEvent.class, (event -> {
            if (!event.getPlayer().level().isClientSide)
                EBAdvancementTriggers.DISCOVER_SPELL.trigger((ServerPlayer) event.getPlayer(), event.getSpell(), event.getSource());
        }));
    }

    private static void onPlayerJoin(WizardryEventBus bus) {
        bus.register(EBPlayerJoinServerEvent.class, (event -> SpellGlyphData.get((ServerLevel) event.getPlayer().level()).sync((ServerPlayer) event.getPlayer())));
        bus.register(EBPlayerJoinServerEvent.class, (SpellProperties::onPlayerJoin));
        bus.register(EBPlayerJoinServerEvent.class, ConfigManager::onPlayerJoin);
    }

    private static void onServerLevelLoad(WizardryEventBus bus) {
        bus.register(EBServerLevelLoadEvent.class, SpellGlyphData::onServerLevelLoad);
        bus.register(EBServerLevelLoadEvent.class, (e) -> {
            ConfigManager.loadServerConfigs(e.getLevel().getServer().getWorldPath(LevelResource.ROOT));
        });
    }

    private static void onServerLoad(WizardryEventBus bus) {

    }

    private static void onEntityJoinLevel(WizardryEventBus bus) {
        bus.register(EBEntityJoinLevelEvent.class, DataEvents::onMinionJoinLevel);
    }

    private static void onItemTossEvent(WizardryEventBus bus) {
        bus.register(EBItemTossEvent.class, DataEvents::onConjureToss);
    }

    private static void onLivingDeathEvent(WizardryEventBus bus) {
        bus.register(EBLivingDeathEvent.class, ArtifactItem::onKillEntity);
        bus.register(EBLivingDeathEvent.class, DataEvents::onConjureEntityDeath);
        bus.register(EBLivingDeathEvent.class, WandUpgradeItem::onPlayerKillMob);
    }

    private static void onItemPlaceInContainer(WizardryEventBus bus) {
        bus.register(EBItemPlaceInContainerEvent.class, DataEvents::onConjureItemPlaceInContainer);
    }

    private static void onSpellPreCast(WizardryEventBus bus) {
        bus.register(SpellCastEvent.Pre.class, WizardArmorItem::onSpellPreCast);
        bus.register(SpellCastEvent.Pre.class, Forfeit::onSpellCastPreEvent);
        bus.register(SpellCastEvent.Pre.class, ArtifactItem::onSpellPreCast);
        bus.register(SpellCastEvent.Pre.class, EBEventHelper::castContextCheck);
        bus.register(SpellCastEvent.Pre.class, FontOfMana::onSpellCastPreEvent);
        bus.register(SpellCastEvent.Pre.class, com.koomplo.wizardry.content.spell.healing.EmpoweringPresence::onSpellCastPreEvent);
        bus.register(SpellCastEvent.Pre.class, ArcaneJammer::onSpellCastPreEvent);
        bus.register(SpellCastEvent.Pre.class, WizardryAttributeModifier::onPreCast);
    }

    private static void onSpellPostCast(WizardryEventBus bus) {
        bus.register(SpellCastEvent.Post.class, Forfeit::onSpellCastPostEvent);
        bus.register(SpellCastEvent.Post.class, ArtifactItem::onSpellPostCast);
    }

    private static void onSpellTickCast(WizardryEventBus bus) {
    }

    private static void onPlayerInteractEntity(WizardryEventBus bus) {
        bus.register(EBPlayerInteractEntityEvent.class, DataEvents::onPlayerInteractMinion);
    }

    private static void onPlayerUseBlock(WizardryEventBus bus) {
        bus.register(EBPlayerUseBlockEvent.class, ArcaneLockSpell::onPlayerUseBlock);
    }

    private static void onPlayerBreakBlock(WizardryEventBus bus) {
        bus.register(EBPlayerBreakBlockEvent.class, ArcaneLockSpell::onPlayerBreakBlock);
    }

    private static void onItemPickup(WizardryEventBus bus) {
        bus.register(EBItemPickupEvent.class, RandomSpellBookItem::onPickup);
    }


    private static void onPlayerItemPickup(WizardryEventBus bus) {
        bus.register(EBPlayerItemPickupEvent.class, (e) -> {
            if (ArtifactChannel.isEquipped(e.getEntity(), EBItems.CHARM_AUTO_SMELT.get())) {
                net.minecraft.world.item.crafting.SingleRecipeInput input = new net.minecraft.world.item.crafting.SingleRecipeInput(e.getItemEntity().getItem());
                Optional<net.minecraft.world.item.crafting.RecipeHolder<SmeltingRecipe>> optionalSmeltingRecipe = e.getEntity().level().getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, e.getEntity().level());
                if (optionalSmeltingRecipe.isPresent()) ArtifactUtils.findMatchingWandAndCast(e.getEntity(), Spells.POCKET_FURNACE);
            }
        });
    }

    private static void castContextCheck(SpellCastEvent.Pre event) {
        boolean enabled = switch (event.getSource()) {
            case WAND -> event.getSpell().isEnabled(SpellContext.WANDS);
            case SCROLL -> event.getSpell().isEnabled(SpellContext.SCROLL);
            case COMMAND -> event.getSpell().isEnabled(SpellContext.COMMANDS);
            case NPC -> event.getSpell().isEnabled(SpellContext.NPCS);
            case DISPENSER -> event.getSpell().isEnabled(SpellContext.DISPENSERS);
            default -> true;
        };

        // If a spell is disabled in the config, it will not work.
        if (!enabled) {
            if (event.getCaster() != null && !event.getCaster().level().isClientSide)
                event.getCaster().sendSystemMessage(Component.translatable("spell.disabled", event.getSpell().getDescriptionFormatted()));
            event.setCanceled(true);
        }
    }
}
